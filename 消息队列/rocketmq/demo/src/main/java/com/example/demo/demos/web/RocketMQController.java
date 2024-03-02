package com.example.demo.demos.web;

import com.example.demo.demos.service.MQProducerService;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Poison02
 * @date 2024/3/2
 */
@RestController
@RequestMapping("/rocketmq")
public class RocketMQController {

    @Autowired
    private MQProducerService mqProducerService;

    @GetMapping("/send")
    public void send() {
        User user = new User();
        user.setName("nameIsOk");
        user.setAge(100);
        mqProducerService.send(user);
    }

    @GetMapping("/sendTag")
    public SendResult sendTag() {
        SendResult sendResult = mqProducerService.sendTagMsg("带有tag的字符消息");
        return sendResult;
    }

}
