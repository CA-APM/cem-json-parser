import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class CemJavaPlugin implements CemPluginApiV2 {
    public ConcurrentHashMap<Integer,String> chunkedRequest = new ConcurrentHashMap<Integer,String>();
    public ConcurrentHashMap<Integer,String> chunkedResponse = new ConcurrentHashMap<Integer,String>();

    /**
     * Initialize the plugin.
     */
    public boolean initialize(int pluginApiVersion) {
        switch (pluginApiVersion) {
          case 2:
              return true;
          default:
              break;
        }
        System.err.println("Unexpected plugin API version " + pluginApiVersion
                           + ": expecting version 2");
        return false;
    }

    /**
     * Check if responseHeader "Content-Type" contains "json".
     */
    public CemPluginOutput responseHeader(int id, String name, String value) {
        if (name.equals("Content-Type")) {
            if (value.indexOf("json") > -1) {
                chunkedResponse.put(new Integer(id),"");
            }
        }
        return null;
    }

    public CemPluginOutput requestHeader(int id, String name, String value) {
        if (name.equals("Content-Type")) {
            if (value.indexOf("json") > -1) {
                chunkedRequest.put(new Integer(id),"");
            }
        }
        return null;
    }

    public CemPluginOutput newComponent(int id,
                                        String httpCommand,
                                        String httpPath,
                                        String httpVersion) {
        return null;
    }

    public CemPluginOutput responseStart(int id,
                                         int httpStatusCode,
                                         String httpVersion,
                                         String httpMessage) {
        return null;
    }

    /**
     * Store all chunks of request body.
     */
    public CemPluginOutput responseBody(int id, byte[] str) {
        try {
            if (str.length == 0) {
                return null;
            }
            
            Integer key = new Integer(id);
            if (chunkedResponse.containsKey(key)) {
                String bodyChunk = new String(str);
                bodyChunk = chunkedResponse.get(key) + bodyChunk;
                chunkedResponse.put(key,bodyChunk);
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }


    public CemPluginOutput requestBody(int id, byte[] str) {
        try {
            if (str.length == 0) {
                return null;
            }
            
            Integer key = new Integer(id);
            if (chunkedRequest.containsKey(key)) {
                String bodyChunk = new String(str);
                bodyChunk = chunkedRequest.get(key) + bodyChunk;
                chunkedRequest.put(key,bodyChunk);
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    public CemPluginOutput endRequestHeader(int id) {
        return null;
    }

    public CemPluginOutput endResponseHeader(int id) {
        return null;
    }

    public CemPluginOutput endRequest(int id) {
        try {
            Integer requestKey = new Integer(id);
            if (!chunkedRequest.containsKey(requestKey)) {
                return null;
            }

            String body = chunkedRequest.get(requestKey);
            chunkedRequest.remove(requestKey);

            String nextObject = cleanString(body);
            Vector<CemPluginOutput.CemParam> outputParams =
                    new Vector<CemPluginOutput.CemParam>();

            while (nextObject != null) {
//                System.out.println(nextObject);
                nextObject = findJson(nextObject, outputParams);
            }
            if (outputParams.size() > 0) {
                CemPluginOutput output =
                        new CemPluginOutput(outputParams.toArray(
                            new CemPluginOutput.CemParam[outputParams.size()]), null);
                return output;
            }
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }

    /**
     * Parse complete request.
     */
    public CemPluginOutput endResponse(int id) {
        try {
            Integer requestKey = new Integer(id);
            if (!chunkedResponse.containsKey(requestKey)) {
                return null;
            }

            String body = chunkedResponse.get(requestKey);
            chunkedResponse.remove(requestKey);

            String nextObject = cleanString(body);
            Vector<CemPluginOutput.CemParam> outputParams =
                    new Vector<CemPluginOutput.CemParam>();

            while (nextObject != null) {
//                System.out.println(nextObject);
                nextObject = findJson(nextObject, outputParams);
            }
            if (outputParams.size() > 0) {
                CemPluginOutput output =
                        new CemPluginOutput(outputParams.toArray(
                            new CemPluginOutput.CemParam[outputParams.size()]), null);
                return output;
            }
        } catch (Exception ex) {
            //ignore
        }
        return null;
    }

    public CemPluginOutput endComponent(int id) {
        return null;
    }

    /**
     * Find all JSON strings and return them as CEM plugin parameters.
     * @param fullString the JSON string
     * @param vec the CEM parameters
     * @return the path above the current Object or null
     */
    public String findJson(String fullString, Vector<CemPluginOutput.CemParam> vec) {
        if (vec == null) {
            return null;
        }
        String currentPath = null; //this is the path above the current Object...

        int currentPairDelim = fullString.lastIndexOf(":");
        if (currentPairDelim == -1) {
            return null;
        }
        int lastObjBound = fullString.indexOf("}",currentPairDelim);


        if (lastObjBound == -1) {
            lastObjBound = fullString.length();
        }
        if (lastObjBound > 0) {
            String value = fullString.substring(currentPairDelim + 1, lastObjBound);
            if (value.startsWith("{")) {
                value = value.substring(1);
            }
//            System.out.println("  value = " + value);

            //find name
            String tempString = fullString.substring(0,currentPairDelim);


            //look for , look for {, look for
            String name = "";
            int commaBound = tempString.lastIndexOf(",");
            int objectBound = tempString.lastIndexOf("{");
            if (commaBound > objectBound) {
                name = tempString.substring(commaBound + 1);
                if (name.startsWith("{")) {
                    name = name.substring(1);
                }
//                System.out.println(" 1 name = " + name);

                currentPath = tempString.substring(0,commaBound);
            } else {

                if (objectBound > -1) {
                    int objectSetDelim = tempString.lastIndexOf(",",currentPairDelim);
                    int objectNameDelim = tempString.lastIndexOf("{",currentPairDelim);

                    if (objectSetDelim > objectNameDelim) {
                        objectNameDelim = objectSetDelim;
                    }
                    if (objectNameDelim == -1) {
                        return null;
                    }
                    name = tempString.substring(objectNameDelim, currentPairDelim);
                    if (name.startsWith("{")) {
                        name = name.substring(1);
                    }
//                    System.out.println(" 2 name = " + name);
                    currentPath = tempString.substring(0,objectNameDelim + 1);
                }

//                if (commaBound == -1) {
//                    name = fullString + "." + name;
//                    System.out.println(" 3  name = " + name);
//                }
            }

            // Finish finding parent path by looking for :{ and } pairs...
            // if there isn't a } then we are a member of that one
            // because of how we clipped things.
            if (currentPath == null) {
                return null;
            }
            String tempPath = currentPath;
//            System.out.println(" tempPath = " + tempPath);
            int objectDelimL = tempPath.lastIndexOf(":{");
            while (objectDelimL > -1) {
                int objectDelimR = tempPath.indexOf("}",objectDelimL);

//              System.out.println(" objectDelimL = " + objectDelimL + ", objectDelimR = " + objectDelimR);

                if (objectDelimR == -1) { //no end }
                    int objectNameDelim = tempPath.lastIndexOf(",", objectDelimL);
                    if (objectNameDelim == -1) {
                        objectNameDelim = tempPath.lastIndexOf("{", objectDelimL);
                    }
                    if (objectNameDelim > -1) {
                        name = tempPath.substring(objectNameDelim,objectDelimL) + "#" + name;
                        tempPath = tempPath.substring(0,objectNameDelim);
                    } else {
                        return null;
                    }
                } else {
                    tempPath = tempPath.substring(0, objectDelimL - 1);
                }
                objectDelimL = tempPath.lastIndexOf(":{");

            }
            //clean name
            name = name.replaceAll("\\{","");
            name = name.replaceAll(",","");
            name = name.replaceAll(":","");
            name = name.trim();
            value = value.trim();

            if (value.length() > 0) {
                CemPluginOutput.CemParam param = new CemPluginOutput.CemParam(name,value);
                vec.add(param);
//                System.out.println("added " + name + " = " + value);
            }

        }

        return currentPath;
    }

    /**
     * This method will clean stuff up so that it's more uniform
     * (i.e remove CR/LF, tabs, extra spaces..., white space.
     * @param str String to clean
     * @return clean String
     */
    public String cleanString(String str) {
        str = str.replaceAll("  ","");
        str = str.replaceAll("\\t","");
        str = str.replaceAll("\\r","");
        str = str.replaceAll("\\n","");
        
        // We don't really care if it's an array, treat it the same as a compound object
        str = str.replaceAll("\\[","{");
        str = str.replaceAll("\\]","}");
        
        str = str.replaceAll(" \"","");
        str = str.replaceAll("\" ","");
        str = str.replaceAll("\"","");
        str = str.replaceAll(": ",":");
        str = str.trim();

        return str;
    }


    public void terminate() {
    }
}
