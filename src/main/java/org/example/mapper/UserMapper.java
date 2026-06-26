package org.example.mapper;

import org.example.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int insertUser(User user);
    User selectUserByUsername(@Param("username") String username);
    User selectUserById(@Param("userId") Integer userId);
    int updateProfile(User user);
}