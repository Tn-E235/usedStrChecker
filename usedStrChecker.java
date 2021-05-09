import java.io.*;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

class usedStrChecker {
    public static String currentDir = null;
    public static Map<String,String> definedStr = new HashMap<String,String>();
    public static Map<String,String> strName = new HashMap<String,String>();
    public static Map<String,String> strPath = new HashMap<String,String>();

    public static int maxLen = 0;
    public static void main(String args[]) {
        File routeFile;
        try {
            if (args.length == 1) {
                routeFile = new File(args[0]);
            } else {
                System.out.println("[ERR1]MapFile is not found.");
                return;
            }

            if (checkBeforeReadfile(routeFile)) {
                String outputFile = "./checked_" + routeFile.getName();
                File fileW = new File(outputFile);
                FileOutputStream fos = new FileOutputStream(fileW);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                PrintWriter pw = new PrintWriter(osw);

                setCurrentDir(routeFile.getPath());
                List<String> usedStrList = search(routeFile.getPath());
                usedStrList.sort(Comparator.naturalOrder());

                pw.println("BveTs Structure List 1.00");

                List<Entry<String, String>> list_entries = new ArrayList<Entry<String, String>>(definedStr.entrySet());
                Collections.sort(list_entries, new Comparator<Entry<String, String>>() {
                    public int compare(Entry<String, String> obj1, Entry<String, String> obj2) {
                        return obj1.getValue().compareTo(obj2.getValue());
                    }
                });
                String b_name = "";
                String b_path = "";
                for (Entry<String,String> entry : list_entries) {
                    if (usedStrList.contains(entry.getKey())) {
                        if (b_path.equals(entry.getValue())) 
                            if (b_name.toLowerCase().equals(entry.getKey().toLowerCase())) 
                                continue;
                        pw.println(String.format("%-"+maxLen+"s", entry.getKey()) +","+ entry.getValue());
                        b_name = entry.getKey();
                        b_path = entry.getValue();
                    }
                }
                pw.close();
                System.out.println("finished");
            } else {
                System.out.println("[ERR2]_file is not found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

    private static boolean isPass(String str) {
        if (str.contains("Structure.Load")){
            definedStr = loadStrList(mapInclude(str));
            return true;
        }
        String[] checkList = { "Repeater", "Structure" , "Background" };
        for (String string : checkList)
            if (str.contains(string))
                return false;
        return true;
    }

    private static String commentCut(String str){
        int idx = -1;
        if (str.contains("//")) idx = str.indexOf("//");
        if(str.contains("#"))   idx = str.indexOf("#");
        if (idx < 0) return str;
        if (idx == 0) return null;
        return str.substring(0,idx-1);
    }

    private static String mapInclude (String path){
        String regex = null;
        if (path.contains("..\\")) regex = "..\\";
        else path = path.replace(".\\","");
        
        int startIdx = path.indexOf("'")+1;
        int endIdx = path.lastIndexOf("'");  
        
        String fileName = path.substring(startIdx, endIdx);
        String dir = currentDir;

        System.out.println(fileName);

        if (regex != null) {
            Pattern p    = Pattern.compile(regex);
            Matcher m    = p.matcher(fileName);
            int matchNum = 0;
            startIdx = fileName.length()-1;
            endIdx   = fileName.length()-1;

            while (m.find()) matchNum++;
            while (matchNum > 0) {
                startIdx = dir.lastIndexOf("\\", endIdx);
                endIdx   = dir.lastIndexOf("\\", startIdx - 1);
                fileName = dir.substring(0, endIdx);
                matchNum = matchNum - 2;
            }
        }
        boolean linux = true;
        if (linux) {
            String tmp = ".\\" + dir + fileName;
            tmp = tmp.replace("\\","/");
            return tmp;
        }
        return currentDir+fileName;
    }

    private static void setCurrentDir(String str) {
        System.out.println(str);
        int idx = str.lastIndexOf("\\");
        currentDir = str.substring(0,idx+1);
    }

    private static List<String> loadSignal(String path) {
        List<String> list = new ArrayList<String>();
        try{
            File fileR = new File(path);
            if (checkBeforeReadfile(fileR)){
                BufferedReader br = new BufferedReader(new FileReader(fileR));
                String str;
                while((str = br.readLine()) != null){
                    String[] signalKey = str.split(",", 0);
                    for (String string : signalKey)
                        if (!list.contains(string))
                            list.add(string);
                }
                br.close();
            } else {
                System.out.println("[loadSignal]file is not found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        }
        return list;
    }
    private static List<String> loadTrain(String path) {
        List<String> list = new ArrayList<String>();
        try{
            File fileR = new File(path);
            if (checkBeforeReadfile(fileR)){
                BufferedReader br = new BufferedReader(new FileReader(fileR));
                String str;
                while((str = br.readLine()) != null){
                    if (!str.toLowerCase().contains("key")) continue;
                    int idx = str.indexOf("=");
                    String key = str.substring(idx+1, str.length());
                    if (!list.contains(key))
                        list.add(key);
                }
                br.close();
            } else {
                System.out.println("[loadTrain]file is not found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        } catch (Exception e) {

        }
        return list;
    }
    private static Map <String,String> loadStrList(String path) {
        Map <String,String> list = new HashMap<String,String>();
        try{
            File fileR = new File(path);
            if (checkBeforeReadfile(fileR)){
                BufferedReader br = new BufferedReader(new FileReader(fileR));
                String str;
                while((str = br.readLine()) != null){
                    String regex = ",";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(str);
                    int matchNum = 0;

                    while (m.find()) matchNum++;

                    int idx = str.indexOf(regex);
                    if (matchNum > 0 && idx > 0) {
                        String name = str.substring(0, idx);
                        int len = name.length();
                        if (len > maxLen) maxLen = len;

                        String comment = "";
                        if (str.contains("//")) {
                            // comment = str.substring(str.indexOf("//"), len);
                            continue;
                        }
                        if (str.contains("#")){
                            // comment = str.substring(str.indexOf("#"), len);
                            continue;
                        }
                        if (!list.containsKey(name))
                            list.put(name.trim(),str.substring(idx + 1, str.length()).trim()+comment);
                        // if (!strName.containsKey(name.toLowerCase())){
                        //     strName.put(name.toLowerCase().trim(), name.trim());
                        //     strPath.put(name.toLowerCase().trim(), str.substring(idx + 1, str.length()).trim()+comment);
                        // }
                        continue;
                    }
                }
                br.close();
            } else {
                System.out.println("[ERR3]file is not found.");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        }
        return list;
    }
    private static List<String> search(String path) {
        List<String> strList = new ArrayList<String>();
        try{
            String str;
            File fileR = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(fileR));
            while((str = br.readLine()) != null){
                str = commentCut(str);
                if (str == null) continue;
                if (str.contains("include")) {
                    for (String string : search(mapInclude(str)))
                        if (!strList.contains(string))
                            strList.add(string);
                    continue;
                }
                if (str.contains("Signal.Load")) {
                    for (String string : loadSignal(mapInclude(str)))
                        if (!strList.contains(string))
                            strList.add(string);
                    continue;
                }
                if (str.contains("Train.Add")) {
                    continue;
                    // for (String string : loadTrain(mapInclude(str)))
                    //     if (!strList.contains(string))
                    //         strList.add(string);
                    // continue;
                }
                if (isPass(str)) continue;
                
                String regex = "'";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(str);
                int matchNum = 0;

                while (m.find()) matchNum++;

                int startIdx = 0;
                int endIdx = 0;
                String strKey = "";
                while (matchNum > 0) {
                    startIdx = str.indexOf(regex, startIdx);
                    endIdx = str.indexOf(regex, startIdx + 1);
                    strKey = str.substring(startIdx + 1, endIdx);

                    if (!strList.contains(strKey)) strList.add(strKey);
                    startIdx = endIdx + 1;
                    matchNum = matchNum - 2;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch(IOException e) {
            System.out.println(e);
        }
        return strList;
    }

    private static boolean checkBeforeReadfile(File file) {
        if (file.exists() && file.isFile() && file.canRead())
            return true;
        return false;
    }
}