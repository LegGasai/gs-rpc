package com.leggasai.rpc.utils;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-10:39
 * @Description:
 */
public class PathUtil {

    public static String buildPath(String splitToken,String ...subPaths) {
        StringBuilder sb = new StringBuilder();
        for (String subPath : subPaths) {
            sb.append(splitToken);
            sb.append(subPath);
        }
        return sb.toString();
    }
}
