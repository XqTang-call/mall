package com.txq.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.txq.bo.AdminUserDetails;
import com.txq.commom.exception.Asserts;
import com.txq.commom.util.RequestUtil;
import com.txq.dao.UmsAdminRoleRelationDao;
import com.txq.mapper.UmsAdminLoginLogMapper;
import com.txq.mapper.UmsAdminMapper;
import com.txq.model.UmsAdmin;
import com.txq.model.UmsAdminExample;
import com.txq.model.UmsAdminLoginLog;
import com.txq.model.UmsResource;
import com.txq.service.UmsAdminCacheService;
import com.txq.service.UmsAdminService;
import com.txq.util.JwtTokenUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@Service
public class UmsAdminServiceImpl implements UmsAdminService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsAdminServiceImpl.class);
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UmsAdminCacheService adminCacheService;
    @Autowired
    private UmsAdminMapper adminMapper;
    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;
    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;

    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            // 获取用户列表的详细资料
            UserDetails userDetails = loadUserByUsername(username);
            if(!passwordEncoder.matches(password,userDetails.getPassword())){
                Asserts.fail("密码不正确");
            }
            if(!userDetails.isEnabled()){
                Asserts.fail("帐号已被禁用");
            }
            // (UsernamePasswordAuthenticationToken:用户名密码身份验证(翻译))
            // 个人理解：通过 token 生成秘钥 （这里跟下文中的方法起了冲突，理解有问题）
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // SecurityContextHolder：安全上下文持有人（翻译）
            //  setAuthentication: 设置身份验证(翻译)
            //  个人理解：把生成的秘钥存储起来
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // generateToken 通过 userDetails 生成 token
            token = jwtTokenUtil.generateToken(userDetails);
//            updateLoginTimeByUsername(username);
            // 添加登记记录
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        // 最后把这个 token 返回出去
        return token;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        //获取用户信息
        UmsAdmin admin = getAdminByUsername(username);
        if (admin != null) {
            // 如果没有获取到则通过 getResourceList 方法获取用户资源列表
            List<UmsResource> resourceList = getResourceList(admin.getId());
            // 返回用户的详细资料
            return new AdminUserDetails(admin,resourceList);
        }
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    @Override
    public UmsAdmin getAdminByUsername(String username) {
        // getAdmin 方法作用：获取缓存后台用户信息（没有找到数据持久层调用数据库相关信息，所以是通过 redis 获取吗？
        //                   怎么获取的）
        UmsAdmin admin = adminCacheService.getAdmin(username);
        if(admin!=null) return  admin;
        UmsAdminExample example = new UmsAdminExample();
        // 这段方法貌似是操作 redis 数据库的方法
        example.createCriteria().andUsernameEqualTo(username);
        // 从数据库中获取数据（但是看不懂他查的是什么）
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if (adminList != null && adminList.size() > 0) {
            admin = adminList.get(0);
            adminCacheService.setAdmin(admin);
            return admin;
        }
        return null;
    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        // getResourceList()：获取缓存后台用户资源列表
        // 通过获取到用户信息的 id，查找缓存后台用户列表，存入到集合当中
        List<UmsResource> resourceList = adminCacheService.getResourceList(adminId);
        if(CollUtil.isNotEmpty(resourceList)){
            // 若集合不为空则直接返回
            return  resourceList;
        }
        // 若为空，则 通过 getResourceList 从数据库中获取用户所有可访问资源，再封装到集合当中
        resourceList = adminRoleRelationDao.getResourceList(adminId);
        if(CollUtil.isNotEmpty(resourceList)){
            // 此时集合不为空则把获取到的数据纳入缓存（应该是）
            adminCacheService.setResourceList(adminId,resourceList);
        }
        // 返回集合
        return resourceList;
    }

    /**
     * 添加登录记录
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if(admin==null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(RequestUtil.getRequestIp(request));
        loginLogMapper.insert(loginLog);
    }
}
