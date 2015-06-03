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
 * �ļ�������
 * 
 * @author jiqinlin
 *
 */
public class FileUtil {
    private static final Logger log = Logger.getLogger(FileUtil.class);
    
    public static final String FILESUFFIX = ".txt";  //�ļ���׺
    
    public static final String DIRPATH = "/sdcard/pressureSD";   // ����Ŀ¼ 
    public static final String RAMPATH = "/data/data/siat.ncu.press.main/siatPM.txt"; //�ֻ��ڴ��ļ�·��
    public static final String SDPATH = "/sdcard/pressureSD/siatPM.txt";   //�ֻ�sd���ļ�·�����ڳ������Ȱ����ݴ�����ڴ��У�Ȼ���ٸ��Ƶ�sd���У� 
    
    /**
     * ��ȡ�ļ����ݣ�ʹ��UTF-8���룩
     * @param filePath ����ļ�·��
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
     * ���ļ�����д���ļ���ʹ��UTF-8���룩
     * @param content �ļ�����
     * @param filePath  ����ļ�·��
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
     * д�ļ�
     * @param outputPath ����ļ�·��
     * @param is ������
     * @param isApend �Ƿ�׷��
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
     * copy�ļ�
     * @param is ������
     * @param outputPath ����ļ�·��
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
     * д�ļ�
     * @param outputPath ����ļ�·��
     * @param inPath �����ļ�·��
     * @throws IOException
     */
    public static void writeFile(String inPath,String outputPath,boolean isAppend) throws IOException{
        if(new File(inPath).exists()){
            FileInputStream fis = new FileInputStream(inPath);
            writeFile(fis,outputPath,isAppend);          
        }else {
            System.out.println("�ļ�copyʧ�ܣ�����Դ�ļ�������!");
        }
    }
    
    /**
     * ���ַ���д���ļ���
     * @param outputPath ����ļ�·��
     * @param msg �ַ���
     * @param isApend  �Ƿ�׷��
     * @throws IOException
     */
    public static void writeContent(String msg,String outputPath,boolean isApend) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath,isApend));
        bw.write(msg);
        bw.flush();
        bw.close();
    }
    
    /**
     * ɾ���ļ����µ���������,�������ļ���
     * @param path ɾ���ļ�·��
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
     * �����д����ļ�����Ŀ¼�����ڣ����ȴ���
     * @param outputPath ����ļ�·��
     */
    public static void createDir(String outputPath){
        File outputFile = new File(outputPath);
        File outputDir = outputFile.getParentFile();
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
    }       

    
    /**
     * ����Ŀ¼
     * 
     * @param dir Ŀ¼
     */
    public static void mkdir(String dir) {
        try {
            String dirTemp = dir;
            File dirPath = new File(dirTemp);
            if (!dirPath.exists()) {
                dirPath.mkdir();
            }
        } catch (Exception e) {
            log.error("����Ŀ¼��������: "+e.getMessage());
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
     * �½��ļ�
     * 
     * @param fileName
     *            String ����·�����ļ��� ��:E:\phsftp\src\123.txt
     * @param content
     *            String �ļ�����
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
            log.error("�½��ļ���������: "+e.getMessage());
            e.printStackTrace();
        }

    }
    
    /**
     * ɾ���ļ�
     * 
     * @param fileName ����·�����ļ���
     */
    public static void delFile(String fileName) throws IOException{
            String filePath = fileName;
            java.io.File delFile = new java.io.File(filePath);
            delFile.delete();
    }
    

    /**
     * ɾ���ļ���
     * 
     * @param folderPath  �ļ���·��
     */
    public static void delFolder(String folderPath) {
        try {
            // ɾ���ļ���������������
            delAllFile(folderPath); 
            String filePath = folderPath;
            java.io.File myFilePath = new java.io.File(filePath);
            // ɾ�����ļ���
            myFilePath.delete(); 
        } catch (Exception e) {
            log.error("ɾ���ļ��в�������"+e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * ɾ���ļ�������������ļ�
     * 
     * @param path �ļ���·��
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
            //File.separator��ϵͳ�йص�Ĭ�����Ʒָ���
            //��UNIXϵͳ�ϣ����ֶε�ֵΪ'/'����Microsoft Windowsϵͳ�ϣ���Ϊ '\'��
            if (path.endsWith(File.separator)) {
                temp = new File(path + childFiles[i]);
            } else {
                temp = new File(path + File.separator + childFiles[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + childFiles[i]);// ��ɾ���ļ���������ļ�
                delFolder(path + "/" + childFiles[i]);// ��ɾ�����ļ���
            }
        }
    }

    
    /**
     * ���Ƶ����ļ�
     * 
     * @param srcFile
     *            ����·����Դ�ļ� �磺E:/phsftp/src/abc.txt
     * @param dirDest
     *            Ŀ���ļ�Ŀ¼�����ļ�Ŀ¼���������Զ�����  �磺E:/phsftp/dest
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
            log.error("�����ļ���������:"+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * �����ļ���
     * 
     * @param oldPath
     *            String Դ�ļ���·�� �磺E:/phsftp/src
     * @param newPath
     *            String Ŀ���ļ���·�� �磺E:/phsftp/dest
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            // ����ļ��в����� ���½��ļ���
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
                if (temp.isDirectory()) {// ��������ļ���
                    copyFolder(oldPath + "/" + files[i], newPath + "/" + files[i]);
                }
            }
        } catch (Exception e) {
            log.error("�����ļ��в�������:"+e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * �ƶ��ļ���ָ��Ŀ¼
     * 
     * @param oldPath ����·�����ļ��� �磺E:/phsftp/src/ljq.txt
     * @param newPath Ŀ���ļ�Ŀ¼ �磺E:/phsftp/dest
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
     * �ƶ��ļ���ָ��Ŀ¼������ɾ���ļ���
     * 
     * @param oldPath Դ�ļ�Ŀ¼  �磺E:/phsftp/src
     * @param newPath Ŀ���ļ�Ŀ¼ �磺E:/phsftp/dest
     */
    public static void moveFiles(String oldPath, String newPath) {
        copyFolder(oldPath, newPath);
        delAllFile(oldPath);
    }
    
    /**
     * �ƶ��ļ���ָ��Ŀ¼����ɾ���ļ���
     * 
     * @param oldPath Դ�ļ�Ŀ¼  �磺E:/phsftp/src
     * @param newPath Ŀ���ļ�Ŀ¼ �磺E:/phsftp/dest
     */
    public static void moveFolder(String oldPath, String newPath) {
        copyFolder(oldPath, newPath);
        delFolder(oldPath);
    }
    
    
    /**
     * ѹ���ļ�
     * 
     * @param srcDir
     *            ѹ��ǰ��ŵ�Ŀ¼
     * @param destDir
     *            ѹ�����ŵ�Ŀ¼
     * @throws Exception
     */
    public static void yaSuoZip(String srcDir, String destDir) throws Exception {
        String tempFileName=null;
        byte[] buf = new byte[1024*2];
        int len;
        //��ȡҪѹ�����ļ�
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
                    ZipOutputStream zos = new ZipOutputStream(bos);// ѹ����

                    ZipEntry ze = new ZipEntry(file.getName());// ѹ�����ļ���
                    zos.putNextEntry(ze);// д���µ�ZIP�ļ���Ŀ��������λ����Ŀ���ݵĿ�ʼ��

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
     * ��ȡ����
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
     * һ��һ�ж�ȡ�ļ����ʺ��ַ���ȡ������ȡ�����ַ�ʱ���������
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