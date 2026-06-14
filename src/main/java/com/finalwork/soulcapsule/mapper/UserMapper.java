package com.finalwork.soulcapsule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finalwork.soulcapsule.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
