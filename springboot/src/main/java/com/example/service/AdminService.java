package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.exception.CustomException;
import com.example.mapper.AdminMapper;
import com.example.utils.PasswordUtils;
import com.example.utils.TokenUtils;
import com.example.utils.ValidateUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    /**
     * 新增
     */
    public void add(Admin admin) {
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        //账号/电话/邮箱格式校验
        if (!ValidateUtils.isValidUsername(admin.getUsername())) {
            throw new CustomException(ResultCodeEnum.USERNAME_FORMAT_ERROR);
        }
        if (!ValidateUtils.isValidPhone(admin.getPhone())) {
            throw new CustomException(ResultCodeEnum.PHONE_FORMAT_ERROR);
        }
        if (!ValidateUtils.isValidEmail(admin.getEmail())) {
            throw new CustomException(ResultCodeEnum.EMAIL_FORMAT_ERROR);
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        admin.setRole(RoleEnum.ADMIN.name());
        //密码 BCrypt 哈希后入库
        admin.setPassword(PasswordUtils.hash(admin.getPassword()));
        adminMapper.insert(admin);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            adminMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Admin admin) {
        //提交了密码时强制哈希，防止异常客户端把明文写入数据库
        if (ObjectUtil.isNotEmpty(admin.getPassword())) {
            admin.setPassword(PasswordUtils.hash(admin.getPassword()));
        }
        //电话、邮箱格式校验（选填，空串视为清空放行）
        if (!ValidateUtils.isValidPhone(admin.getPhone())) {
            throw new CustomException(ResultCodeEnum.PHONE_FORMAT_ERROR);
        }
        if (!ValidateUtils.isValidEmail(admin.getEmail())) {
            throw new CustomException(ResultCodeEnum.EMAIL_FORMAT_ERROR);
        }
        //账号仅在发生变更时校验格式并查重，兼容未满足新规则的存量账号
        if (ObjectUtil.isNotEmpty(admin.getUsername()) && admin.getId() != null) {
            Admin dbAdmin = adminMapper.selectById(admin.getId());
            if (dbAdmin != null && !admin.getUsername().equals(dbAdmin.getUsername())) {
                if (!ValidateUtils.isValidUsername(admin.getUsername())) {
                    throw new CustomException(ResultCodeEnum.USERNAME_FORMAT_ERROR);
                }
                Admin exist = adminMapper.selectByUsername(admin.getUsername());
                if (exist != null && !exist.getId().equals(admin.getId())) {
                    throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
                }
            }
        }
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID查询
     */
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    /**
     * 分页查询
     */
    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public Account login(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        //密码比对（库存为存量明文时自动兼容）
        if (!PasswordUtils.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        //存量明文密码透明升级：比对成功后立即哈希回写
        if (!PasswordUtils.isHashed(dbAdmin.getPassword())) {
            dbAdmin.setPassword(PasswordUtils.hash(account.getPassword()));
            adminMapper.updateById(dbAdmin);
        }
        // 生成token（以库存密码哈希作为签名密钥）
        String tokenData = dbAdmin.getId() + "-" + RoleEnum.ADMIN.name();
        String token = TokenUtils.createToken(tokenData, dbAdmin.getPassword());
        dbAdmin.setToken(token);
        dbAdmin.setPassword(null); // 哈希不回传前端
        return dbAdmin;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!PasswordUtils.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        //新密码哈希后入库（存量明文密码随之升级为哈希）
        dbAdmin.setPassword(PasswordUtils.hash(account.getNewPassword()));
        adminMapper.updateById(dbAdmin);
    }

}