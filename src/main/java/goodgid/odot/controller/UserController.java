package goodgid.odot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import goodgid.odot.common.response.CommonResponse;
import goodgid.odot.common.response.enums.ReturnCode;
import goodgid.odot.model.User;
import goodgid.odot.repository.oltp.UserMapper;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("user")
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public CommonResponse<?> getUser() {
        User user = userMapper.selectByPrimaryKey(1L);
        return new CommonResponse<>(ReturnCode.SUCCESS, user);
    }
}
