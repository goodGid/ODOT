package goodgid.odot.repository.oltp;

import goodgid.odot.model.dao.UserDao;

public interface UserMapper {

    UserDao selectByPrimaryKey(Long userSequence);

    UserDao selectByName(String name, String nickName);
}
