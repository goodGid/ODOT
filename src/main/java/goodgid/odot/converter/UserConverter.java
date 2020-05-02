package goodgid.odot.converter;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import goodgid.odot.model.dao.UserDao;
import goodgid.odot.model.dto.request.UserDto;
import goodgid.odot.repository.oltp.UserMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserConverter {

    @Component
    public static class StringToPersonConverter implements Converter<String, UserDto> {

        @Autowired
        private UserMapper userMapper;

        @Value("${author:'goodgid'}")
        private String default_name;

        @Override
        public UserDto convert(String name) {
            if (StringUtils.isBlank(name)) {
                name = default_name;
            }

            UserDto userDto = new UserDto();
            UserDao userDao = userMapper.selectByName(name, name);
            try {
                BeanUtils.copyProperties(userDao, userDto);
            } catch (Exception e) {
                log.error("DB has no corresponding `{}` value.", name);
                log.error(e.toString());
            }
            return userDto;
        }
    }
}