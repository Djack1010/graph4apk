import CG.CG;
import CG.CG.CGData;
import completePDG.cPDG;
import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.*;
import java.util.*;

public class createCG {

  //TEST, set to 0 for analyze all classes and methods
  private static int MAX_TEST_CLASS = 0;
  private static int MAX_TEST_METH = 0;
  private static String CLASS_TO_TEST = ""; //"com.google.update.RU"; //"cn.domob.android.ads.q1";
  private static String METH_TO_TEST = ""; //"U1"; //"run";
  private static Set<String> METH_JAIL = new HashSet<>(Arrays.asList("com.admogo.DataBackup_getDataList"));
  private static CG cg = new CG();

  public static class settings {
    public static String SDGFileName = null;
    public static String SDGLabel = null;
    public static String rootPath = "/home/djack/local_repositories/graph4apk";
    public static String outputPath = "/home/djack/local_repositories/graph4apk";
    public static boolean genJimple = false;
    public settings() {}
    public void printSettings(){
      System.out.println("SETTINGS FOR ANALYSIS:");
      System.out.println("ROOT PATH: " + this.rootPath);
      System.out.println("OUTPUT PATH: " + this.outputPath);
      System.out.println("GENERATE JIMPLE: " + this.genJimple);
    }
  }

  private static int uniqueIndex=1;
  private static settings runningSettings = new settings();

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
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-16/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-15/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-14/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-13/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-12/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-11/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-10/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-9/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-8/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-7/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-6/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-5/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-4/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-3/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-17/android.jar" +
          ":" + runningSettings.rootPath + "/src/main/resources/android-platforms/android-17/android-17-api.jar",
        "-android-jars",
        "" + runningSettings.rootPath + "/src/main/resources/android-platforms",
        //"-process-dir",
        //"" + runningSettings.rootPath + "/apk_db/OK/0ad370eab2ac647a932ad18fbb55d098.apk",
        "-process-dir",
        "" + runningSettings.rootPath + "/apk_db_AMD/Airpush/0a1fc800465e64f9deaa063b81688509.apk"
      };
    } else
      sootArgs = handleArgs(args);

    if (runningSettings.SDGFileName == null) {
      runningSettings.SDGLabel = "NO_LABEL";
      runningSettings.SDGFileName = "anSDG";
    }

    if (runningSettings.rootPath == null) {
      System.err.println("ERROR! Set the project path, exiting...");
      System.exit(1);
    }

    //prefer Android APK files// -src-prec apk
    Options.v().set_src_prec(Options.src_prec_apk);

    // resolve the PrintStream and System soot-classes
    Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
    Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

    runningSettings.printSettings();

    PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter", new SceneTransformer() {

      @Override
      protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

        System.out.println("STARTING MY TRANSFORMATION");

        int numTestClas = 0;
        int progrBar = 1;

        for (SootClass cl : Scene.v().getApplicationClasses()) {

          progressBar(progrBar,Scene.v().getApplicationClasses().size());
          progrBar++;

          if (!CLASS_TO_TEST.equals("") && !cl.getName().replaceAll("\\$", "DDOLLARO").equals(CLASS_TO_TEST))
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

            if (!METH_TO_TEST.equals("") && !m.getName().replaceAll("\\$", "DDOLLARO").equals(METH_TO_TEST))
              continue;

            if (MAX_TEST_METH != 0 && numTestMeth >= MAX_TEST_METH)
              break;

            if (METH_JAIL.contains(cl.getName().replaceAll("\\$", "DDOLLARO") + "_"
              + m.getName().replaceAll("\\$", "DDOLLARO")))
              continue;

            //System.out.println("\t\tmethod " + m.getName());

            String fileName = cl.getName() + "_" +
              m.getName() +
              m.getParameterTypes().toString().replaceAll(" ", "")
                .replaceAll("(?<!(byte|java.lang.String|java.lang.Object|\\[\\]|int|boolean))\\[", "(")
                .replaceAll("(?<!(byte|java.lang.String|java.lang.Object|int|boolean|\\[\\])\\[)\\]", ")");

            fileName = fileName.replaceAll("\\$", "DDOLLARO");

            String fileNameForStoring = fileName;
            if (fileName.length() > 100)
              fileNameForStoring = fileName.substring(0, 99);


            if (!(m.hasActiveBody())) {
              //System.err.println("\t\t\tNo active body for method " + m.getName());
              cg.addNotActiveBody(fileName);
              continue;
            }

            Body body = m.retrieveActiveBody();

            numTestMeth++;

            if (runningSettings.genJimple) {
              //Print Jimple code of Body method on file
              StringWriter sw = new StringWriter();
              PrintWriter pw = new PrintWriter(sw);
              Printer.v().printTo(body, pw);
              //String inputString = "public class Foo extends java.lang.Object {\n" + sw.toString() + "}";
              try {
                checkAndCreateFolder(runningSettings.outputPath
                  + "/code/JimpleCode/" + runningSettings.SDGFileName);
                //if (checkFileExist(outputPath + "/code/JimpleCode/" + fileName + ".jimple"))
                //System.err.println("FILE " + fileName + ".jimple ALREADY EXIST!");
                PrintWriter out = new PrintWriter(runningSettings.outputPath + "/code/JimpleCode/" + runningSettings.SDGFileName
                  + "/M" + uniqueIndex + ".jimple", "UTF-8");
                out.println(sw.toString());
                out.close();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }


            //Represents a CFG where the nodes are Unit instances,
            // and where no edges are included to account for control flow associated with exceptions.
            //UnitGraph cfg = new BriefUnitGraph(body);
            UnitGraph cfg = new ExceptionalUnitGraph(body);
            //UnitGraph cfg = new TrapUnitGraph(body);

            //System.out.print("\t\t\tGENERATING cPDG...");
            cPDG cPDG = new cPDG(cfg, fileName, cl.getName(), m.getName(), uniqueIndex, true);
            uniqueIndex++;

            if (cPDG.isIndexable())
              cg.addcPDG(cPDG);
            else
              cg.addFailedPDG(cPDG);

          }

        }

      }

    }));

    //RUN SOOT
    soot.Main.main(sootArgs);

    String result = cg.matchInvokecPDG();
    System.out.println(result);

    checkAndCreateFolder(runningSettings.outputPath + "/stats");
    try (PrintWriter out = new PrintWriter(runningSettings.outputPath + "/stats/" + runningSettings.SDGFileName + ".txt")) {
      out.println(result);
      System.out.println("Result print on file '" + runningSettings.outputPath + "/stats/"
        + runningSettings.SDGFileName + ".txt'");
    } catch (FileNotFoundException e) {
      System.err.println(e);
      System.exit(1);
    }

    CGData cg_data = cg.genCG();

    checkAndCreateFolder(runningSettings.outputPath + "/graphs/CG/" + runningSettings.SDGLabel + "/" +
            runningSettings.SDGFileName );
    try (PrintWriter out = new PrintWriter(runningSettings.outputPath + "/graphs/CG/" +
            runningSettings.SDGLabel + "/" + runningSettings.SDGFileName + "/nodes.txt")) {
      out.println(cg_data.nodeXfeatures);
      System.out.println("CG print on file '" + runningSettings.outputPath + "/graphs/CG/" +
              runningSettings.SDGLabel + "/" + runningSettings.SDGFileName + "/nodes.txt'");
    } catch (FileNotFoundException e) {
      System.err.println(e);
      System.exit(1);
    }
    try (PrintWriter out = new PrintWriter(runningSettings.outputPath + "/graphs/CG/" +
            runningSettings.SDGLabel + "/" + runningSettings.SDGFileName + "/edges.txt")) {
      out.println(cg_data.edges);
      System.out.println("CG print on file '" + runningSettings.outputPath + "/graphs/CG/" +
              runningSettings.SDGLabel + "/" + runningSettings.SDGFileName + "/edges.txt'");
    } catch (FileNotFoundException e) {
      System.err.println(e);
      System.exit(1);
    }

    //sdg.completeAnalysis();

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
          runningSettings.SDGFileName=args[i];
          break;
        case "-SDGLabel":
          i++;
          runningSettings.SDGLabel=args[i];
          break;
        case "-projectPath":
          i++;
          runningSettings.rootPath=args[i];
          runningSettings.outputPath=runningSettings.rootPath + "/results";
          break;
        case "-genJimple":
          runningSettings.genJimple=true;
          break;
        case "-silent":
          PrintStream dummyStream = new PrintStream(new OutputStream(){
            public void write(int b) {
              ; // NO-OP
            }
          });
          System.setOut(dummyStream);
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

  private static void progressBar(int partial, int total){
    int relative_partial = (partial*33)/total;
    for(int i=0; i<70;i++){
      System.out.print(" ");
    }
    System.out.print("\n[");
    for(int i=0; i<relative_partial;i++){
      System.out.print("#");
    }
    for(int i=0; i<33-relative_partial;i++){
      System.out.print("-");
    }
    System.out.println("] " + partial + "\\" + total);
    if(partial != total)
      System.out.print("\033[2A");
  }

}
