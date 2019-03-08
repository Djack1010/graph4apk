import soot.*;
import soot.options.Options;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.graph.pdg.ProgramDependenceGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.SmartLocalDefs;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import utility.PDGToDotGraph;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class createPDG {

    //PATHs
    private static String outputPath="/home/giacomo/IdeaProjects/graph4apk/results";

    //TEST, set to 0 for analyze all classes and methods
    private static int MAX_TEST_CLAS = 3;
    private static int MAX_TEST_METH = 3;

    public static void main(String [] args) {

        String[] sootArgs=null;

        if (args.length == 0) {
            //pass arguments for Soot.Main
            sootArgs = new String[]{
                    //"-allow-phantom-refs",
                    "-pp",
                    "-no-bodies-for-excluded",
                    //"-full-resolver",
                    "-w",
                    "-cp",
                    "." +
                            //":/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar" +
                            //":/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-16/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-15/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-14/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-13/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-12/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-11/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-10/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-9/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-8/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-7/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-6/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-5/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-4/android.jar" +
                            //":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-3/android.jar" +
                            ":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-17/android.jar" +
                            ":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-17/android-17-api.jar",
                    "-android-jars",
                    "/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms",
                    "-process-dir",
                    "/home/giacomo/Documents/merc_proj/apk_test/0d4a16a36a62e4d9bc6e466729a55094.apk"
            };
        }



        //prefer Android APK files// -src-prec apk
        Options.v().set_src_prec(Options.src_prec_apk);

        //output as APK, too//-f J
        //Options.v().set_output_format(Options.output_format_dex);

        // resolve the PrintStream and System soot-classes
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

                System.out.println("STARTING MY TRANSFORMATION");

                int numTestClas=0;

                for (SootClass cl : Scene.v().getApplicationClasses()) {

                    int numTestMeth=0;
                    if(MAX_TEST_CLAS != 0 && numTestClas >= MAX_TEST_CLAS)
                        break;
                    else
                        numTestClas++;

                    System.out.println("\tStarting Transformation for class " + cl.getName());

                    Iterator<SootMethod> methodIt = cl.getMethods().iterator();
                    while (methodIt.hasNext()) {

                        if(MAX_TEST_METH != 0 && numTestMeth >= MAX_TEST_METH)
                            break;

                        SootMethod m = methodIt.next();
                        System.out.println("\t\tmethod " + m.getName());

                        if (!(m.hasActiveBody())) {
                            System.err.println("\t\t\tNo active body for method " + m.getName());
                            continue;
                        }

                        Body body = m.retrieveActiveBody();
                        numTestMeth++;

                        //Represents a CFG where the nodes are Unit instances,
                        // and where no edges are included to account for control flow associated with exceptions.
                        //TODO: move to better CFG to handle exceptions (see ExceptionalUnitGraph or TrapUnitGraph)
                        System.out.print("\t\t\tGENERATING CFG...");
                        UnitGraph cfg = new BriefUnitGraph(body);
                        System.out.println("SUCCESS!");
                        System.out.println("\t\t\t\tPrinting CFG on file");
                        CFGToDotGraph cfgToDot = new CFGToDotGraph();
                        DotGraph CFGdotGraph = cfgToDot.drawCFG(cfg, body);
                        checkAndCreateFolder(outputPath + "/graphs/CFGs");
                        CFGdotGraph.plot(outputPath + "/graphs/CFGs/" + cl.getName() + "_" + m.getName() + ".dot");

                        System.out.println("\t\t\t\tDEF-USE CHAIN:");
                        createPDGDataEdge(cfg);

                        System.out.print("\t\t\tGENERATING PDG...");
                        ProgramDependenceGraph pdg = new HashMutablePDG(cfg);
                        System.out.println("SUCCESS!");
                        //Print on file the pdg using PDGToDotGraph
                        System.out.println("\t\t\t\tPrinting PDG on file");
                        PDGToDotGraph pdgToDot = new PDGToDotGraph(pdg, m.getName());
                        DotGraph PDGdotGraph = pdgToDot.drawPDG();
                        checkAndCreateFolder(outputPath + "/graphs/PDGs");
                        PDGdotGraph.plot(outputPath + "/graphs/PDGs/" + cl.getName() + "_" + m.getName() + ".dot");

                        //TODO: Aggiungere def-use chain per completare il PDG

                    }

                }

            }

        }));

        //RUN SOOT
        soot.Main.main(sootArgs);
    }

    private static void checkAndCreateFolder(String folderPath){
        File directory = new File(folderPath);
        if (! directory.exists()){
            directory.mkdirs();
        }
    }

    private static void createPDGDataEdge(UnitGraph graph){
        SimpleLiveLocals s = new SimpleLiveLocals(graph);
        Iterator<Unit> gIt = graph.iterator();
        // generate du-pairs
        while (gIt.hasNext()) {

            Unit defUnit = gIt.next();
            SmartLocalDefs des = new SmartLocalDefs(graph, s); // defs of local variables
            SimpleLocalUses uses = new SimpleLocalUses(graph, des);

            List<UnitValueBoxPair> ul = uses.getUsesOf(defUnit);
            if (ul != null && ul.size() != 0) {
                Iterator<UnitValueBoxPair> iteraBoxPair=ul.iterator();
                while(iteraBoxPair.hasNext()) {
                    Unit useUnit = iteraBoxPair.next().getUnit();
                    System.err.println("\t\t\t\t\tDEF: " + defUnit.toString() + " USE: " + useUnit.toString());
                }
            }
        }
    }


}

