package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.DbUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.PasswordUtils;
import com.example.utils.TokenUtils;
import com.example.utils.ValidateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.List;

/**
 * 功能：
 * 作者：captain_dong
 * 日期：2023/12/19 21:46
 */
@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    public void register(Account account) {
        User user=new User();
        BeanUtils.copyProperties(account,user);
        this.add(user);
    }

    /**
     * 新增用户
     * @param user
     */
    public void add(User user){
        //业务方法
        //1.判断用户账号是否重复
        User dbUser = userMapper.selectByUserName(user.getUsername());
        if (dbUser!= null){
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        //1.1账号格式校验（4-16位字母、数字或下划线）
        if (!ValidateUtils.isValidUsername(user.getUsername())) {
            throw new CustomException(ResultCodeEnum.USERNAME_FORMAT_ERROR);
        }
        //1.2电话、邮箱格式校验（选填，填了必须合法）
        if (!ValidateUtils.isValidPhone(user.getPhone())) {
            throw new CustomException(ResultCodeEnum.PHONE_FORMAT_ERROR);
        }
        if (!ValidateUtils.isValidEmail(user.getEmail())) {
            throw new CustomException(ResultCodeEnum.EMAIL_FORMAT_ERROR);
        }
        //2.密码为空时使用默认密码
        if (ObjectUtil.isEmpty(user.getPassword())){
            user.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        //3.判断用户名称是否为空
        if (ObjectUtil.isEmpty(user.getName())){
            user.setName(user.getUsername());
        }
        //4.默认用户角色
        user.setRole(RoleEnum.USER.name());

        //4.1 记录注册时间（仪表盘用户增长趋势）
        user.setCreateTime(DateUtil.now());

        //5.密码 BCrypt 哈希后入库
        user.setPassword(PasswordUtils.hash(user.getPassword()));
        userMapper.insert(user);
    }

    /**
     * 删除
     * @param id
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除
     * @param ids
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    /**
     * 更新用户信息
     * @param user
     */
    public void updateById(User user) {
        //提交了密码时强制哈希，防止异常客户端把明文写入数据库
        if (ObjectUtil.isNotEmpty(user.getPassword())) {
            user.setPassword(PasswordUtils.hash(user.getPassword()));
        }
        //电话、邮箱格式校验（选填，空串视为清空放行）
        if (!ValidateUtils.isValidPhone(user.getPhone())) {
            throw new CustomException(ResultCodeEnum.PHONE_FORMAT_ERROR);
        }
        if (!ValidateUtils.isValidEmail(user.getEmail())) {
            throw new CustomException(ResultCodeEnum.EMAIL_FORMAT_ERROR);
        }
        //账号仅在发生变更时校验格式并查重，兼容未满足新规则的存量账号
        if (ObjectUtil.isNotEmpty(user.getUsername()) && user.getId() != null) {
            User dbUser = userMapper.selectById(user.getId());
            if (dbUser != null && !user.getUsername().equals(dbUser.getUsername())) {
                if (!ValidateUtils.isValidUsername(user.getUsername())) {
                    throw new CustomException(ResultCodeEnum.USERNAME_FORMAT_ERROR);
                }
                User exist = userMapper.selectByUserName(user.getUsername());
                if (exist != null && !exist.getId().equals(user.getId())) {
                    throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
                }
            }
        }
        userMapper.updateById(user);
    }

    /**
     * 根据用户ID查询用户信息
     * @param id
     * @return
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有用户
     * @param user
     * @return
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询用户
     * @param user
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<User> users = userMapper.selectAll(user);
        return PageInfo.of(users);
        //return new PageInfo<>(userMapper.selectAll(user));
    }

    /**
     * 用户登录逻辑
     * @param account
     * @return
     */
    public Account login(Account account) {
        User dbUser = userMapper.selectByUserName(account.getUsername());
        //用户是否为空
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        //密码比对（库存为存量明文时自动兼容）
        if (!PasswordUtils.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        //存量明文密码透明升级：比对成功后立即哈希回写
        if (!PasswordUtils.isHashed(dbUser.getPassword())) {
            dbUser.setPassword(PasswordUtils.hash(account.getPassword()));
            userMapper.updateById(dbUser);
        }
        // 生成token（以库存密码哈希作为签名密钥）
        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        dbUser.setPassword(null); // 哈希不回传前端
        return dbUser;
    }

    /**
     * 用户修改密码
     * @param account
     */
    public void updatePassword(Account account) {
        User bdUser = userMapper.selectByUserName(account.getUsername());
        if (ObjectUtil.isNull(bdUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!PasswordUtils.matches(account.getPassword(), bdUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        //新密码哈希后入库（存量明文密码随之升级为哈希）
        bdUser.setPassword(PasswordUtils.hash(account.getNewPassword()));
        userMapper.updateById(bdUser);
    }
}