package goodgid.odot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ObjectEncryptionUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger("ObjectEncryptionUti");

    public Object encryptObject(Object originObject) {

        Object clonedObject = null;

        try {
            // To Do Encryption Work.
            // ex) BeanUtils.copyProperties(originObject, clonedObject);
            // ex) String encryptedName = encrypt(clonedObject.getName());
            // ex) clonedObject.setName(encryptedName)
        } catch (Exception e) {
            LOGGER.warn("Encrypt Fail : " + e);
        }
        return originObject;
    }
}