package dan;

import java.util.Map;

/**
 * @author: Dennis
 * @date: 2020/6/16 13:00
 */

public interface SendSms {
    public boolean send(String phoneNum, String templateCode, Map<String,Object> code);

}
