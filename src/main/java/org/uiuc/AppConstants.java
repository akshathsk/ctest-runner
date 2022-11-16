package org.uiuc;

import java.util.HashMap;
import java.util.Map;

public interface AppConstants {

  Map<String, String> moduleToDirMap = new HashMap<String, String>() {
    {
      put("oap-server/server-starter", "./oap-server/server-starter/src/main/resources/");
      put("oap-server/server-configuration/configuration-apollo", "./oap-server/server-configuration/configuration-apollo/src/test/resources/");
      put("oap-server/server-configuration/configuration-etcd", "./oap-server/server-configuration/configuration-etcd/src/test/resources/");
      put("oap-server/server-configuration/configuration-zookeeper", "./oap-server/server-configuration/configuration-zookeeper/src/test/resources/");
    }
  };

  String ERROR_MSG = "Error on exec() method";
}
