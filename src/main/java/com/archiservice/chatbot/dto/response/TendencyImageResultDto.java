package com.archiservice.chatbot.dto.response;

import com.archiservice.chatbot.dto.type.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TendencyImageResultDto {
  @JsonProperty("user_id")
  private String user_id;
  private String summary;
  private String tags;

  @JsonProperty("message_type")
  private MessageType message_type;
}
