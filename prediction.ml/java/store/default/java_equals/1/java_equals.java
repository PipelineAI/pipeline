
  private String str;

  public void initialize(Map<String, Object> args) {
  }

  public Object predict(Map<String, Object> inputs) {
      String id = (String)inputs.get("id");

      return id.equals("21619");
  }
