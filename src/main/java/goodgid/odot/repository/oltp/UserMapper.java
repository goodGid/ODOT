package goodgid.odot.repository.oltp;

import goodgid.odot.model.User;

public interface UserMapper {

    User selectByPrimaryKey(Long userSequence);
}
