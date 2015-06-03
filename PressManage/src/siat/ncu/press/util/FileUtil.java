package siat.ncu.press.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

/**
 * 文件工具类
 * 
 * @author jiqinlin
 *
 */
public class FileUtil {
    private static final Logger log = Logger.getLogger(FileUtil.class);
    
    public static final String FILESUFFIX = ".txt";  //文件后缀
    
    public static final String DIRPATH = "/sdcard/pressureSD";   // 创建目录 
    public static final String RAMPATH = "/data/data/siat.ncu.press.main/siatPM.txt"; //手机内存文件路径
    public static final String SDPATH = "/sdcard/pressureSD/siatPM.txt";   //手机sd卡文件路径（在程序中先把数据存放在内存中，然后再复制到sd卡中） 
    
    /**
     * 读取文件内容（使用UTF-8编码）
     * @param filePath 输出文件路径
     * @return
     * @throws Exception
     */
    public static String readFileUTF8(String filePath) throws Exception{
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis,"UTF-8"));
        String fileContent = "";
        String temp = "";
        while ((temp = br.readLine()) != null) {
            fileContent = fileContent+temp;
        }
        br.close();
        fis.close();
        return fileContent;
    }
    
    public static boolean isExitFile(String filePath) {
        File file = new File(filePath);
        if(file.exists()) {
           return true; 
        }else {
            return false;
        }
    }
    
    
    
    /**
     * 将文件内容写入文件（使用UTF-8编码）
     * @param content 文件内容
     * @param filePath  输出文件路径
     * @throws Exception
     */
    public static void writeFileUTF8(String content,String filePath) throws Exception{
        createDir(filePath);
        File file = new File(filePath);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
        bw.write(content);
        bw.flush();
        bw.close();
        fos.close();
    }
    
    /**
     * 写文件
     * @param outputPath 输出文件路径
     * @param is 输入流
     * @param isApend 是否追加
     * @throws IOException
     */
    public static void writeFile(InputStream is,String outputPath,boolean isApend) throws IOException{
        FileInputStream fis = (FileInputStream)is;
        createDir(outputPath);
        FileOutputStream fos = new FileOutputStream(outputPath,isApend);
        byte[] bs = new byte[1024*16];
        int len = -1;
        while ((len = fis.read(bs)) != -1) {
            fos.write(bs, 0, len);
        }
        fos.close();
        fis.close();
    }
    
    /**
     * copy文件
     * @param is 输入流
     * @param outputPath 输出文件路径
     * @throws Exception
     */
    public static void writeFile(InputStream is,String outputPath) throws Exception{
        InputStream bis=null;
        OutputStream bos=null;
        createDir(outputPath);
        bis=new BufferedInputStream(is);
        bos=new BufferedOutputStream(new FileOutputStream(outputPath));
        byte[] bs = new byte[1024*10];
        int len = -1;
        while ((len = bis.read(bs)) != -1) {
            bos.write(bs, 0, len);
        }
        bos.flush();
        bis.close();
        bos.close();
    }
    
    /**
     * 写文件
     * @param outputPath 输出文件路径
     * @param inPath 输入文件路径
     * @throws IOException
     */
    public static void writeFile(String inPath,String outputPath,boolean isAppend) throws IOException{
        if(new File(inPath).exists()){
            FileInputStream fis = new FileInputStream(inPath);
            writeFile(fis,outputPath,isAppend);          
        }else {
            System.out.println("文件copy失败，由于源文件不存在!");
        }
    }
    
    /**
     * 将字符串写到文件内
     * @param outputPath 输出文件路径
     * @param msg 字符串
     * @param isApend  是否追加
     * @throws IOException
     */
    public static void writeContent(String msg,String outputPath,boolean isApend) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath,isApend));
        bw.write(msg);
        bw.flush();
        bw.close();
    }
    
    /**
     * 删除文件夹下的所有内容,包括本文件夹
     * @param path 删除文件路径
     * @throws IOException
     */
    public static void delFileOrDerectory(String path)throws IOException{ 
        File file = new File(path); 
        if(file.exists()){ 
            if(file.isDirectory()){ 
                File[] files = file.listFiles(); 
                for(int i=0;i<files.length;i++){ 
                    File subFile = files[i]; 
                    delFileOrDerectory(subFile.getAbsolutePath());
                } 
                file.delete(); 
            }else{ 
                file.delete(); 
            } 
        } 
    }
    
    /**
     * 如果欲写入的文件所在目录不存在，需先创建
     * @param outputPath 输出文件路径
     */
    public static void createDir(String outputPath){
        File outputFile = new File(outputPath);
        File outputDir = outputFile.getParentFile();
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
    }       

    
    /**
     * 创建目录
     * 
     * @param dir 目录
     */
    public static void mkdir(String dir) {
        try {
            String dirTemp = dir;
            File dirPath = new File(dirTemp);
            if (!dirPath.exists()) {
                dirPath.mkdir();
            }
        } catch (Exception e) {
            log.error("创建目录操作出错: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean isExitDir(String dir) {
        File dirPath = new File(dir);
        if (dirPath.exists()) {
            return true;
        }else {
            return false;
        }
    }


    /**
     * 新建文件
     * 
     * @param fileName
     *            String 包含路径的文件名 如:E:\phsftp\src\123.txt
     * @param content
     *            String 文件内容
     *            
     */
    public static void createNewFile(String fileName, String content) {
        try {
            String fileNameTemp = fileName;
            File filePath = new File(fileNameTemp);
            if (!filePath.exists()) {
                filePath.createNewFile();
            }
            FileWriter fw = new FileWriter(filePath);
            PrintWriter pw = new PrintWriter(fw);
            String strContent = content;
            pw.println(strContent);
            pw.flush();
            pw.close();
            fw.close();
        } catch (Exception e) {
            log.error("新建文件操作出错: "+e.getMessage());
            e.printStackTrace();
        }

    }
    
    /**
     * 删除文件
     * 
     * @param fileName 包含路径的文件名
     */
    public static void delFile(String fileName) throws IOException{
            String filePath = fileName;
            java.io.File delFile = new java.io.File(filePath);
            delFile.delete();
    }
    

    /**
     * 删除文件夹
     * 
     * @param folderPath  文件夹路径
     */
    public static void delFolder(String folderPath) {
        try {
            // 删除文件夹里面所有内容
            delAllFile(folderPath); 
            String filePath = folderPath;
            java.io.File myFilePath = new java.io.File(filePath);
            // 删除空文件夹
            myFilePath.delete(); 
        } catch (Exception e) {
            log.error("删除文件夹操作出错"+e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * 删除文件夹里面的所有文件
     * 
     * @param path 文件夹路径
     */
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] childFiles = file.list();
        File temp = null;
        for (int i = 0; i < childFiles.length; i++) {
            //File.separator与系统有关的默认名称分隔符
            //在UNIX系统上，此字段的值为'/'；在Microsoft Windows系统上，它为 '\'。
            if (path.endsWith(File.separator)) {
                temp = new File(path + childFiles[i]);
            } else {
                temp = new File(path + File.separator + childFiles[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + childFiles[i]);// 先删除文件夹里面的文件
                delFolder(path + "/" + childFiles[i]);// 再删除空文件夹
            }
        }
    }

    
    /**
     * 复制单个文件
     * 
     * @param srcFile
     *            包含路径的源文件 如：E:/phsftp/src/abc.txt
     * @param dirDest
     *            目标文件目录；若文件目录不存在则自动创建  如：E:/phsftp/dest
     * @throws IOException
     */
    public static void copyFile(String srcFile, String dirDest) {
        try {
            FileInputStream in = new FileInputStream(srcFile);
            mkdir(dirDest);
            FileOutputStream out = new FileOutputStream(dirDest+"/"+new File(srcFile).getName());
            int len;
            byte buffer[] = new byte[1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            out.close();
            in.close();
        } catch (Exception e) {
            log.error("复制文件操作出错:"+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 复制文件夹
     * 
     * @param oldPath
     *            String 源文件夹路径 如：E:/phsftp/src
     * @param newPath
     *            String 目标文件夹路径 如：E:/phsftp/dest
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            // 如果文件夹不存在 则新建文件夹
            mkdir(newPath);
            File file = new File(oldPath);
            String[] files = file.list();
            File temp = null;
            for (int i = 0; i < files.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + files[i]);
                } else {
                    temp = new File(oldPath + File.separator + files[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath
                            + "/" + (temp.getName()).toString());
                    byte[] buffer = new byte[1024 * 2];
                    int len;
                    while ((len = input.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {// 如果是子文件夹
                    copyFolder(oldPath + "/" + files[i], newPath + "/" + files[i]);
                }
            }
        } catch (Exception e) {
            log.error("复制文件夹操作出错:"+e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 移动文件到指定目录
     * 
     * @param oldPath 包含路径的文件名 如：E:/phsftp/src/ljq.txt
     * @param newPath 目标文件目录 如：E:/phsftp/dest
     */
    public static void moveFile(String oldPath, String newPath) {
        copyFile(oldPath, newPath);
        try {
            delFile(oldPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 移动文件到指定目录，不会删除文件夹
     * 
     * @param oldPath 源文件目录  如：E:/phsftp/src
     * @param newPath 目标文件目录 如：E:/phsftp/dest
     */
    public static void moveFiles(String oldPath, String newPath) {
        copyFolder(oldPath, newPath);
        delAllFile(oldPath);
    }
    
    /**
     * 移动文件到指定目录，会删除文件夹
     * 
     * @param oldPath 源文件目录  如：E:/phsftp/src
     * @param newPath 目标文件目录 如：E:/phsftp/dest
     */
    public static void moveFolder(String oldPath, String newPath) {
        copyFolder(oldPath, newPath);
        delFolder(oldPath);
    }
    
    
    /**
     * 压缩文件
     * 
     * @param srcDir
     *            压缩前存放的目录
     * @param destDir
     *            压缩后存放的目录
     * @throws Exception
     */
    public static void yaSuoZip(String srcDir, String destDir) throws Exception {
        String tempFileName=null;
        byte[] buf = new byte[1024*2];
        int len;
        //获取要压缩的文件
        File[] files = new File(srcDir).listFiles();
        if(files!=null){
            for (File file : files) {
                if(file.isFile()){
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    if (destDir.endsWith(File.separator)) {
                        tempFileName = destDir + file.getName() + ".zip";
                    } else {
                        tempFileName = destDir + "/" + file.getName() + ".zip";
                    }
                    FileOutputStream fos = new FileOutputStream(tempFileName);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    ZipOutputStream zos = new ZipOutputStream(bos);// 压缩包

                    ZipEntry ze = new ZipEntry(file.getName());// 压缩包文件名
                    zos.putNextEntry(ze);// 写入新的ZIP文件条目并将流定位到条目数据的开始处

                    while ((len = bis.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                        zos.flush();
                    }
                    bis.close();
                    zos.close();
                
                }
            }
        }
    }
    
    
    /**
     * 读取数据
     * 
     * @param inSream
     * @param charsetName
     * @return
     * @throws Exception
     */
    public static String readData(InputStream inSream, String charsetName) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while( (len = inSream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inSream.close();
        return new String(data, charsetName);
    }
    
    /**
     * 一行一行读取文件，适合字符读取，若读取中文字符时会出现乱码
     * 
     * @param path
     * @return
     * @throws Exception
     */
    public static Set<String> readFile(String path) throws Exception{
        Set<String> datas=new HashSet<String>();
        FileReader fr=new FileReader(path);
        BufferedReader br=new BufferedReader(fr);
        String line=null;
        while ((line=br.readLine())!=null) {
            datas.add(line);
        }
        br.close();
        fr.close();
        return datas;
    }
}