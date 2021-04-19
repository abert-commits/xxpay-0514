package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.ISysService;
import org.xxpay.service.dao.mapper.*;
import java.util.Date;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/1/2
 * @description:
 */
@Service(version = "1.0.0")
public class SysServiceImpl implements ISysService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysResourceMapper sysResourceMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public int add(SysUser sysUser) {
        return sysUserMapper.insertSelective(sysUser);
    }

    @Override
    public int update(SysUser sysUser) {
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    @Override
    public SysUser findByUserId(Long userId) {
        return sysUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public SysUser findByUserName(String userName) {
        SysUserExample example = new SysUserExample();
        SysUserExample.Criteria criteria = example.createCriteria();
        criteria.andUserNameEqualTo(userName);
        List<SysUser> sysUserList = sysUserMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(sysUserList)) return null;
        return sysUserList.get(0);
    }

    @Override
    public List<SysUser> select(int offset, int limit, SysUser sysUser) {
        SysUserExample example = new SysUserExample();
        example.setOffset(offset);
        example.setLimit(limit);
        example.setOrderByClause("createTime DESC");
        SysUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, sysUser);
        return sysUserMapper.selectByExample(example);
    }

    @Override
    public Integer count(SysUser sysUser) {
        SysUserExample example = new SysUserExample();
        SysUserExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, sysUser);
        return sysUserMapper.countByExample(example);
    }

    @Override
    public Integer batchDelete(List<Long> userIds) {
        for(Long userId : userIds) {
            sysUserMapper.deleteByPrimaryKey(userId);
        }
        return userIds.size();
    }

    @Override
    public List<SysResource> selectAllResource(Byte system) {
        System.out.println("===-----=="+system);
        SysResourceExample example = new SysResourceExample();
        example.setOrderByClause("orderNum asc");
        SysResourceExample.Criteria criteria = example.createCriteria();
        if(system != null) criteria.andSystemEqualTo(system);
        criteria.andStatusEqualTo(MchConstant.PUB_YES);
        return sysResourceMapper.selectByExample(example);
    }

    @Override
    public List<SysResource> selectResourceByUserId(Long userId) {
        return sysResourceMapper.selectResourceByUserId(userId);
    }

    @Override
    public SysResource findResourceById(Long resourceId) {
        return sysResourceMapper.selectByPrimaryKey(resourceId);
    }

    @Override
    public int addResource(SysResource sysResource) {
        return sysResourceMapper.insertSelective(sysResource);
    }

    @Override
    public int updateResource(SysResource sysResource) {
        return sysResourceMapper.updateByPrimaryKeySelective(sysResource);
    }

    @Override
    public int batchDeleteResource(List<Long> resourceIds) {
        if(CollectionUtils.isEmpty(resourceIds)) return 0;
        int deleteCount = 0;
        for(Long resourceId : resourceIds) {
            deleteCount = deleteCount + sysResourceMapper.deleteByPrimaryKey(resourceId);
        }
        return deleteCount;
    }

    @Override
    public int addRole(SysRole sysRole) {
        return sysRoleMapper.insertSelective(sysRole);
    }

    @Override
    public int updateRole(SysRole sysRole) {
        return sysRoleMapper.updateByPrimaryKeySelective(sysRole);
    }

    @Override
    public int batchDeleteRole(List<Long> roleIds) {
        if(CollectionUtils.isEmpty(roleIds)) return 0;
        int deleteCount = 0;
        for(Long roleId : roleIds) {
            deleteCount = deleteCount + sysRoleMapper.deleteByPrimaryKey(roleId);
        }
        return deleteCount;
    }

    @Override
    public SysRole findRoleById(Long roleId) {
        return sysRoleMapper.selectByPrimaryKey(roleId);
    }

    @Override
    public List<SysRole> selectRole(int offset, int limit, SysRole sysRole) {
        SysRoleExample example = new SysRoleExample();
        example.setLimit(limit);
        example.setOffset(offset);
        example.setOrderByClause("createTime desc");
        SysRoleExample.Criteria criteria = example.createCriteria();
        setCriteriaRole(criteria, sysRole);
        return sysRoleMapper.selectByExample(example);
    }

    @Override
    public List<SysRole> selectAllRole() {
        SysRoleExample example = new SysRoleExample();
        example.setOrderByClause("createTime desc");
        return sysRoleMapper.selectByExample(example);
    }

    @Override
    public Integer countRole(SysRole sysRole) {
        SysRoleExample example = new SysRoleExample();
        SysRoleExample.Criteria criteria = example.createCriteria();
        setCriteriaRole(criteria, sysRole);
        return sysRoleMapper.countByExample(example);
    }

    @Override
    public List<SysPermission> selectPermissionByRoleId(Long roleId) {
        SysPermissionExample example = new SysPermissionExample();
        SysPermissionExample.Criteria criteria = example.createCriteria();
        criteria.andRoleIdEqualTo(roleId);
        return sysPermissionMapper.selectByExample(example);
    }

    @Override
    public List<SysUserRole> selectUserRoleByUserId(Long userId) {
        SysUserRoleExample example = new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        return sysUserRoleMapper.selectByExample(example);
    }

    @Override
    public List<SysResource> selectResource(int offset, int limit, SysResource sysResource) {
        SysResourceExample example = new SysResourceExample();
        example.setLimit(limit);
        example.setOffset(offset);
        example.setOrderByClause("createTime desc");
        SysResourceExample.Criteria criteria = example.createCriteria();
        setCriteriaResource(criteria, sysResource);
        return sysResourceMapper.selectByExample(example);
    }

    @Override
    public Integer countResource(SysResource sysResource) {
        SysResourceExample example = new SysResourceExample();
        SysResourceExample.Criteria criteria = example.createCriteria();
        setCriteriaResource(criteria, sysResource);
        return sysResourceMapper.countByExample(example);
    }

    // 保证事务
    @Override
    public void savePermission(Long roleId, List<Long> resourceIdList) {
        // 先删除该角色所有资源
        SysPermissionExample example = new SysPermissionExample();
        SysPermissionExample.Criteria criteria = example.createCriteria();
        criteria.andRoleIdEqualTo(roleId);
        sysPermissionMapper.deleteByExample(example);
        // 保存该角色新的资源
        for(Long resourceId : resourceIdList) {
            SysPermission sysPermission = new SysPermission();
            sysPermission.setRoleId(roleId);
            sysPermission.setResourceId(resourceId);
            sysPermission.setCreateTime(new Date());
            sysPermission.setUpdateTime(new Date());
            sysPermissionMapper.insert(sysPermission);
        }
    }

    @Override
    public void saveUserRole(Long userId, List<Long> roleIds) {
        // 先删除该用户的所有角色
        SysUserRoleExample example = new SysUserRoleExample();
        SysUserRoleExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        sysUserRoleMapper.deleteByExample(example);
        // 保存该用户的新角色
        for(Long roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setUpdateTime(new Date());
            sysUserRoleMapper.insert(sysUserRole);
        }
    }

    void setCriteria(SysUserExample.Criteria criteria, SysUser sysUser) {
        if(sysUser != null) {
            if(sysUser.getUserId() != null) criteria.andUserIdEqualTo(sysUser.getUserId());
            if(sysUser.getUserName() != null) criteria.andUserNameLike("%" + sysUser.getUserName() + "%");
            if(sysUser.getStatus() != null && sysUser.getStatus().byteValue() != -99) criteria.andStatusEqualTo(sysUser.getStatus());
        }
    }

    void setCriteriaRole(SysRoleExample.Criteria criteria, SysRole sysRole) {
        if(sysRole != null) {
            if(sysRole.getRoleName() != null) criteria.andRoleNameLike("%" + sysRole.getRoleName() + "%");
        }
    }

    void setCriteriaResource(SysResourceExample.Criteria criteria, SysResource sysResource) {
        if(sysResource != null) {
            if(sysResource.getSystem() != null && sysResource.getSystem().byteValue() != -99) criteria.andSystemEqualTo(sysResource.getSystem());
            if(sysResource.getTitle() != null) criteria.andTitleLike("%" + sysResource.getTitle() + "%");
            if(sysResource.getStatus() != null && sysResource.getStatus().byteValue() != -99) criteria.andStatusEqualTo(sysResource.getStatus());
        }
    }

}
