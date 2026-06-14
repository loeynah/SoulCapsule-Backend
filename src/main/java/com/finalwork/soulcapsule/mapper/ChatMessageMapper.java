package com.finalwork.soulcapsule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finalwork.soulcapsule.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
