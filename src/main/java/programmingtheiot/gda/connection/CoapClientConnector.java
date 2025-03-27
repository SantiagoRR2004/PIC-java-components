/**
 * This class is part of the Programming the Internet of Things project.
 *
 * <p>It is provided as a simple shell to guide the student and assist with implementation for the
 * Programming the Internet of Things exercises, and designed to be modified by the student as
 * needed.
 */
package programmingtheiot.gda.connection;

import java.util.logging.Logger;
import programmingtheiot.common.IDataMessageListener;
import programmingtheiot.common.ResourceNameEnum;

/** Shell representation of class for student implementation. */
public class CoapClientConnector implements IRequestResponseClient {
  // static

  private static final Logger _Logger = Logger.getLogger(CoapClientConnector.class.getName());

  // params

  // constructors

  /**
   * Default.
   *
   * <p>All config data will be loaded from the config file.
   */
  public CoapClientConnector() {}

  /**
   * Constructor.
   *
   * @param host
   * @param isSecure
   * @param enableConfirmedMsgs
   */
  public CoapClientConnector(String host, boolean isSecure, boolean enableConfirmedMsgs) {}

  // public methods

  @Override
  public boolean sendDiscoveryRequest(int timeout) {
    return false;
  }

  @Override
  public boolean sendDeleteRequest(
      ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
    return false;
  }

  @Override
  public boolean sendGetRequest(
      ResourceNameEnum resource, String name, boolean enableCON, int timeout) {
    return false;
  }

  @Override
  public boolean sendPostRequest(
      ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
    return false;
  }

  @Override
  public boolean sendPutRequest(
      ResourceNameEnum resource, String name, boolean enableCON, String payload, int timeout) {
    return false;
  }

  @Override
  public boolean setDataMessageListener(IDataMessageListener listener) {
    return false;
  }

  public void clearEndpointPath() {}

  public void setEndpointPath(ResourceNameEnum resource) {}

  @Override
  public boolean startObserver(ResourceNameEnum resource, String name, int ttl) {
    return false;
  }

  @Override
  public boolean stopObserver(ResourceNameEnum resourceType, String name, int timeout) {
    return false;
  }

  // private methods

}
