package com.advancedspark.serving.prediction.python

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

object TestPythonRuntime {
  def main(args: Array[String]): Unit = {
    try{
      val p = Runtime.getRuntime().exec(s"python data/python-simple/simple.py")

      val stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

      val stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      // read the output from the command
      System.out.println(s"${stdInput.lines().collect(Collectors.joining("\n"))}, Error ${stdError.lines().collect(Collectors.joining("\n"))}")       
    } catch { 
        case e: Throwable => {
          System.out.println(e)
          throw e
        }
    } 
  }
}