package net.nanisl.zabuton.example;

import net.nanisl.zabuton.app.login.LoginUser;

public class ExampleUser implements LoginUser {
    private static final long serialVersionUID = 1L;

    final private String id;
    private String name;

    public ExampleUser(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User [id=" + this.id + ", name=" + this.name + "]";
    }



//  private static final String DATANAME = "user";
//  private static final String ID_LABEL = "ID";
//  private static final String NAME_LABEL = "名前";

//  public List<LoginUser> getUsers() {
//      List<LoginUser> users = Generics.newArrayList();
//      List<Map<String, String>> maps = new ZabuTextManager().readFiles(DATANAME);
//      for (Map<String, String> map: maps) {
//          LoginUser user = new LoginUser(map.get(ID_LABEL));
//          user.setName(map.get(NAME_LABEL));
//          users.add(user);
//      }
//      return users;
//  }
//  private LoginUser getUser(String id) {
//      for (LoginUser user: getUsers()) {
//          if (StringUtils.equals(user.getId(), id)) {
//              return user;
//          }
//      }
//      return null;
//  }

//  public LoginUser addNewUser(String name) {
//      int maxId = 0;
//      for (LoginUser user: getUsers()) {
//          String id = user.getId();
//          if (StringUtils.isNumeric(id)) {
//              maxId = Math.max(Integer.parseInt(id), maxId);
//          }
//      }
//      int nextId = maxId + 1;
//
//      /* 重複を避ける */
//      String strUserId = String.valueOf(nextId);
////      int sub = 1;
////      while(getUsers(strUserId) != null) {
////          String ex = ExcelSequencer.num2alphabet(sub);
////          strUserId = strUserId + "_" + ex;
////          sub ++;
////      }
//      if (getUser(strUserId) != null) {
//          throw new RuntimeException("ID=" + strUserId + " はすでに登録されている。");
//      }
//
//      LoginUser user = new LoginUser(strUserId);
//      user.setName(name);
//
//      LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(); // 項目の追加順を維持する
//      map.put(ID_LABEL, user.getId());
//      map.put(NAME_LABEL, user.getName());
//
//      /* ファイルの保存 */
//      String fileName = user.getId() + "_" + user.getName();
//      new ZabuTextManager().writeFile(DATANAME, fileName, map);
//
//      return user;
//  }


}
