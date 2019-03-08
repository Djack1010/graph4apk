import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;

import java.util.Iterator;
import java.util.Map;

public class Main {

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
        Options.v().set_output_format(Options.output_format_dex);

        // resolve the PrintStream and System soot-classes
        Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {
                final PatchingChain units = b.getUnits();
                //important to use snapshotIterator here
                for(Iterator iter = units.snapshotIterator(); iter.hasNext();) {
                    final Unit u = (Unit) iter.next();
                    u.apply(new AbstractStmtSwitch() {

                        public void caseInvokeStmt(InvokeStmt stmt) {

                            System.err.println("test");

                            /**
                            InvokeExpr invokeExpr = stmt.getInvokeExpr();
                            if(invokeExpr.getMethod().getName().equals("onDraw")) {

                                Local tmpRef = addTmpRef(b);
                                Local tmpString = addTmpString(b);

                                // insert "tmpRef = java.lang.System.out;"
                                units.insertBefore(Jimple.v().newAssignStmt(
                                        tmpRef, Jimple.v().newStaticFieldRef(
                                                Scene.v().getField("").makeRef())), u);

                                // insert "tmpLong = 'HELLO';"
                                units.insertBefore(Jimple.v().newAssignStmt(tmpString,
                                        StringConstant.v("HELLO")), u);

                                // insert "tmpRef.println(tmpString);"
                                SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream")
                                        .getMethod("void     println(java.lang.String)");
                                units.insertBefore(Jimple.v().newInvokeStmt(
                                        Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString)), u);

                                //check that we did not mess up the Jimple
                                b.validate();
                            }
                            **/
                        }

                    });
                }
            }
        }));

        //RUN SOOT
        soot.Main.main(sootArgs);
    }

    private static Local addTmpRef(Body body) {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    private static Local addTmpString(Body body) {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String"));
        body.getLocals().add(tmpString);
        return tmpString;
    }
}