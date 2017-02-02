
  public Map<String, Object> data = new HashMap<String, Object>();

  public void initialize(Map<String, Object> args) {
    data.put("url", "http://demo.pipeline.io:9040/prediction/");
  }

  public Object predict(Map<String, Object> inputs) {
    try {
      String userId = (String)inputs.get("userId");
      String itemId = (String)inputs.get("itemId");
      String url = data.get("url") + "/" + userId + "/" + itemId;

      return org.apache.http.client.fluent.Request
        .Get(url)
        .execute()
        .returnContent();

    } catch(Exception exc) {
        System.out.println(exc);
        throw new RuntimeException(exc);
    }
  }
