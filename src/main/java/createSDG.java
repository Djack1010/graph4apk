import SDG.SDG;
import completePDG.cPDG;
import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import utility.PDGToDotGraph;
import utility.cPDGToDotGraph;

import java.io.*;
import java.util.*;

public class createSDG {

  //PATHs
  private static String outputPath = "/home/djack/local_repositories/graph4apk/results";

  //TEST, set to 0 for analyze all classes and methods
  private static int MAX_TEST_CLASS = 0;
  private static int MAX_TEST_METH = 0;
  private static String CLASS_TO_TEST = ""; //"com.google.update.RU"; //"cn.domob.android.ads.q1";
  private static String METH_TO_TEST = ""; //"U1"; //"run";
  private static Set<String> METH_JAIL = new HashSet<>(Arrays.asList("com.admogo.DataBackup_getDataList"));
  private static SDG sdg = new SDG();

  private static String SDGFileName = null;

  public static void main(String[] args) {

    String[] sootArgs = null;

    if (args.length == 0) {
      //pass arguments for Soot.Main
      sootArgs = new String[]{
        //"-allow-phantom-refs",
        "-p",
        "cg",
        //"-pp",
        "all-reachable:true",
        "-no-bodies-for-excluded",
        //"-full-resolver",
        "-w",
        "-cp",
        "." +
          //":/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar" +
          //":/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-16/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-15/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-14/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-13/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-12/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-11/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-10/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-9/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-8/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-7/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-6/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-5/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-4/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-3/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-17/android.jar" +
          ":/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms/android-17/android-17-api.jar",
        "-android-jars",
        "/home/djack/local_repositories/graph4apk/src/main/resources/android-platforms",
        "-process-dir",
        //"/home/giacomo/Documents/merc_proj/apk_db/toTest/2EED7318CA564A909E75AD616CAD5CDF.apk"
        "/home/djack/local_repositories/graph4apk/apk_db/OK/0d4a16a36a62e4d9bc6e466729a55094.apk"
      };
    } else
      sootArgs = handleArgs(args);


    //prefer Android APK files// -src-prec apk
    Options.v().set_src_prec(Options.src_prec_apk);

    //output as APK, too//-f J
    //Options.v().set_output_format(Options.output_format_dex);

    // resolve the PrintStream and System soot-classes
    Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
    Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

    PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter", new SceneTransformer() {

      @Override
      protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

        System.out.println("STARTING MY TRANSFORMATION");

        int numTestClas = 0;
        int uniqueIndex=1;

        for (SootClass cl : Scene.v().getApplicationClasses()) {

          if (!CLASS_TO_TEST.equals("") && !cl.getName().replaceAll("\\$", "").equals(CLASS_TO_TEST))
            continue;

          int numTestMeth = 0;
          if (MAX_TEST_CLASS != 0 && numTestClas >= MAX_TEST_CLASS)
            break;
          else
            numTestClas++;

          //System.out.println("\tStarting Transformation for class " + cl.getName());

          Iterator<SootMethod> methodIt = cl.getMethods().iterator();
          while (methodIt.hasNext()) {

            SootMethod m = methodIt.next();

            if (!METH_TO_TEST.equals("") && !m.getName().replaceAll("\\$", "").equals(METH_TO_TEST))
              continue;

            if (MAX_TEST_METH != 0 && numTestMeth >= MAX_TEST_METH)
              break;

            if (METH_JAIL.contains(cl.getName().replaceAll("\\$", "") + "_" + m.getName().replaceAll("\\$", "")))
              continue;

            //System.out.println("\t\tmethod " + m.getName());

            String fileName = cl.getName() + "_" +
              m.getName() +
              m.getParameterTypes().toString().replaceAll(" ", "")
                .replaceAll("(?<!(byte|java.lang.String|java.lang.Object|\\[\\]|int|boolean))\\[", "(")
                .replaceAll("(?<!(byte|java.lang.String|java.lang.Object|int|boolean|\\[\\])\\[)\\]", ")");

            fileName = fileName.replaceAll("\\$", "");

            if (!(m.hasActiveBody())) {
              //System.err.println("\t\t\tNo active body for method " + m.getName());
              sdg.addNotActiveBody(fileName);
              continue;
            }

            Body body = m.retrieveActiveBody();

            numTestMeth++;

            //Print Jimple code of Body method on file
            /**
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            Printer.v().printTo(body, pw);
            //String inputString = "public class Foo extends java.lang.Object {\n" + sw.toString() + "}";
            try {
              checkAndCreateFolder(outputPath + "/graphs/JimpleCode");
              //if (checkFileExist(outputPath + "/graphs/JimpleCode/" + fileName + ".jimple"))
              //System.err.println("FILE " + fileName + ".jimple ALREADY EXIST!");
              PrintWriter out = new PrintWriter(outputPath + "/graphs/JimpleCode/"
                + fileName + ".jimple", "UTF-8");
              out.println(sw.toString());
              out.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
             **/

            //Represents a CFG where the nodes are Unit instances,
            // and where no edges are included to account for control flow associated with exceptions.
            //TODO: move to better CFG to handle exceptions (see ExceptionalUnitGraph or TrapUnitGraph)
            //System.out.print("\t\t\tGENERATING CFG...");
            //UnitGraph cfg = new BriefUnitGraph(body);
            UnitGraph cfg = new ExceptionalUnitGraph(body);
            //UnitGraph cfg = new TrapUnitGraph(body);
            ///**
            //System.out.println("SUCCESS!");
            //CFGToDotGraph cfgToDot = new CFGToDotGraph();
            //DotGraph CFGdotGraph = cfgToDot.drawCFG(cfg, body);
            //checkAndCreateFolder(outputPath + "/graphs/CFGs");
            //CFGdotGraph.plot(outputPath + "/graphs/CFGs/" + fileName + ".dot");
            //**/

            //System.out.print("\t\t\tGENERATING PDG...");
            //ProgramDependenceGraph pdg = new HashMutablePDG(cfg);
            //System.out.println("SUCCESS!");
            //Print on file the pdg using PDGToDotGraph
            //System.out.println("\t\t\t\tPrinting PDG on file");
            //PDGToDotGraph pdgToDot = new PDGToDotGraph(pdg, m.getName());
            //DotGraph PDGdotGraph = pdgToDot.drawPDG();
            //checkAndCreateFolder(outputPath + "/graphs/PDGs");
            //PDGdotGraph.plot(outputPath + "/graphs/PDGs/" + fileName + ".dot");

            //System.out.print("\t\t\tGENERATING cPDG...");
            cPDG cPDG = new cPDG(cfg, fileName, cl.getName(), m.getName(), uniqueIndex);
            uniqueIndex++;
            //System.out.println("SUCCESS!");
            //cPDGToDotGraph cpdgToDot = new cPDGToDotGraph(cPDG.getRootNode(), cPDG.getName());
            //DotGraph cPDGdotGraph = cpdgToDot.drawcPDG();
            //checkAndCreateFolder(outputPath + "/graphs/cPDGs");
            //cPDGdotGraph.plot(outputPath + "/graphs/cPDGs/" + cPDG.getName() + ".dot");

            if ( cPDG.isBuilt() )
              sdg.addcPDG(cPDG);
            else
              sdg.addFailedPDG(cPDG);

            //System.out.print("\t\t\t\tGENERATING CCS...");
            //String ccs = cPDG.generateCCS();
            //System.out.println("SUCCESS!");
            //checkAndCreateFolder(outputPath + "/graphs/CCS");
            //try (PrintWriter out = new PrintWriter(outputPath + "/graphs/CCS/" + fileName + ".ccs")) {
            //  out.println(ccs);
            //} catch (FileNotFoundException e){
            //  System.err.println(e);
            //  System.exit(1);
            //}

          }

        }

      }

    }));

    //RUN SOOT
    soot.Main.main(sootArgs);

    sdg.matchInvokecPDG();

    if (SDGFileName == null){
      SDGFileName="anSDG";
    }

    DotGraph SDGdotGraph = sdg.drawcSDG();
    //checkAndCreateFolder(outputPath + "/graphs/SDG");
    //SDGdotGraph.plot(outputPath + "/graphs/SDG/" + SDGFileName + ".dot");

    String ccs = sdg.generateCCS();
    checkAndCreateFolder(outputPath + "/graphs/CCS");
    try (PrintWriter out = new PrintWriter(outputPath + "/graphs/CCS/" + SDGFileName + ".ccs")) {
      out.println(ccs);
    } catch (FileNotFoundException e){
      System.err.println(e);
      System.exit(1);
    }
    String simpleCcs = sdg.generateSimpleCCS();
    checkAndCreateFolder(outputPath + "/graphs/CCS");
    try (PrintWriter out = new PrintWriter(outputPath + "/graphs/CCS/" + SDGFileName + "simple.ccs")) {
      out.println(simpleCcs);
    } catch (FileNotFoundException e){
      System.err.println(e);
      System.exit(1);
    }
    String ccsNEW = sdg.generateCCS_NEW();
    checkAndCreateFolder(outputPath + "/graphs/CCS");
    try (PrintWriter out = new PrintWriter(outputPath + "/graphs/CCS/" + SDGFileName + "NEW.ccs")) {
        out.println(ccsNEW);
    } catch (FileNotFoundException e){
        System.err.println(e);
        System.exit(1);
    }
    String simpleCcsNEW = sdg.generateSimpleCCS_NEW();
    checkAndCreateFolder(outputPath + "/graphs/CCS");
    try (PrintWriter out = new PrintWriter(outputPath + "/graphs/CCS/" + SDGFileName + "simpleNEW.ccs")) {
      out.println(simpleCcsNEW);
    } catch (FileNotFoundException e){
      System.err.println(e);
      System.exit(1);
    }

  }

  private static void checkAndCreateFolder(String folderPath) {
    File directory = new File(folderPath);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  private static boolean checkFileExist(String path) {
    File f = new File(path);
    return f.exists() && !f.isDirectory();
  }

  private static String[] handleArgs(String[] args){
    String [] myArrayArgs = new String[args.length];
    int i=0;
    int j=0;
    while(i<args.length){
      switch (args[i]){
        case "-p":
        case "cg":
        case "-pp":
        case "all-reachable:true":
        case "-w":
        case "-no-bodies-for-excluded":
        case "-full-resolver":
          myArrayArgs[j]=args[i];
          j++;
          break;
        case "-cp":
        case "-process-dir":
        case "-android-jars":
          myArrayArgs[j]=args[i];
          i++;
          j++;
          myArrayArgs[j]=args[i];
          j++;
          break;
        case "-mainClass":
          i++;
          myArrayArgs[j]=args[i];
          j++;
          break;
        case "-SDGFileName":
          i++;
          SDGFileName=args[i];
          break;
        default:
          System.err.println("MainCPG:ERROR:Invalid arguments " + args[i] + ", exiting...");
          System.exit(0);
          break;
      }
      i++;
    }
    if(j!=myArrayArgs.length){
      String[] tempArray = new String[j];
      System.arraycopy( myArrayArgs, 0, tempArray, 0, tempArray.length );
      return tempArray;
    }else return myArrayArgs;
  }

}
