package be.re.net;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;



public class BasicUser implements User

{

  private String		password;
  private PropertyChangeSupport	propertyChange =
    new PropertyChangeSupport(this);
  private String		username;



  public
  BasicUser()
  {
    this(null, null);
  }



  public
  BasicUser(String username, String password)
  {
    this.username = username;
    this.password = password;
  }



  public void
  addPropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChange.addPropertyChangeListener(listener);
  }



  public void
  addPropertyChangeListener
  (
    String			propertyName,
    PropertyChangeListener	listener
  )
  {
    propertyChange.addPropertyChangeListener(propertyName, listener);
  }



  public String
  getPassword()
  {
    return password;
  }



  public String
  getUsername()
  {
    return username;
  }



  public void
  removePropertyChangeListener(PropertyChangeListener listener)
  {
    propertyChange.removePropertyChangeListener(listener);
  }



  public void
  removePropertyChangeListener
  (
    String			propertyName,
    PropertyChangeListener	listener
  )
  {
    propertyChange.removePropertyChangeListener(propertyName, listener);
  }



  public void
  setPassword(String value)
  {
    if
    (
      (
        value == null		&&
        password == null
      )				||
      (
        value != null		&&
        value.equals(password)
      )
    )
    {
      return;
    }

    String	old = password;

    password = value;
    propertyChange.firePropertyChange("password", old, value);
  }



  public void
  setUsername(String value)
  {
    if
    (
      (
        value == null		&&
        username == null
      )				||
      (
        value != null		&&
        value.equals(username)
      )
    )
    {
      return;
    }

    String	old = username;

    username = value;
    propertyChange.firePropertyChange("username", old, value);
  }

} // BasicUser
