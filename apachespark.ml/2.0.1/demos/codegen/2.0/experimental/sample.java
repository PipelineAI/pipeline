public class DecisionTree_Generated {
  public static 
  int classify(double feature0, double feature1) {
    if (feature1 <= 0.1242251999855711)
      if (feature0 <= 0.3250169286704234)
        if (feature0 <= 0.22501171984875468)
          return 2;
        else if (feature0 > 0.22501171984875468)
          return 0;
       else if (feature0 > 0.3250169286704234)
         return 4;
     else if (feature1 > 0.1242251999855711)
       if (feature0 <= 0.45002343969750935)
         if (feature0 <= 0.22501171984875468)
           return 1;
         else if (feature0 > 0.22501171984875468)
           return 0;
       else if (feature0 > 0.45002343969750935)
       return 1;
  }
}

