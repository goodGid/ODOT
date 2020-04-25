package goodgid.odot.model.dao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDao {

    private Long user_seq;

    private String name;

    private String nick_name;
}
