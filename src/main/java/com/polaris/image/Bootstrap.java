package com.polaris.image;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.polaris.image.core.ImageRecognition;
import com.polaris.image.core.VideoRecord;
import com.polaris.image.service.FaceService;
import com.polaris.image.service.ProgressBar;
import com.polaris.image.util.CommonUtil;
import com.polaris.image.util.GeneralContants;
import com.polaris.image.util.ImageUtil;
import com.polaris.image.util.PropertiesUtil;

public class Bootstrap {
    private int frameWidth = 350;
    private int frameHeight = 250;
    private double startPositionX = (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - frameWidth) / 2;
    private double startPositionY = (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - frameHeight) / 2;
    JButton btnGo;
    JPanel panel;
    
    /**
     * {
     * 创建并显示GUI。出于线程安全的考虑，
     * 这个方法在事件调用线程中调用。
     */
    private void createAndShowGUI() {
        // 确保一个漂亮的外观风格
        JFrame.setDefaultLookAndFeelDecorated(true);

        // 创建及设置窗口
        JFrame frame = new JFrame("视频转换器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds((int) startPositionX, (int) startPositionY, frameWidth, frameHeight);    //设置窗口大小和位置
        // 添加 "Hello World" 标签
        JLabel label = new JLabel("Hello World");
        Container container = frame.getContentPane();    //获取当前窗口的内容窗格
        container.add(label);

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        panel = new JPanel();
        frame.add(panel);
        panel.setBackground(Color.white);    //设置背景色
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        mainPlaceComponents(panel);

        // 调整窗口
        //frame.pack();
        // 设置窗口是否可见
        frame.setVisible(true);
    }
    JButton fileInButton;
    JTextField fileInText;
    JButton fileOutButton;
    JTextField fileOutText;
    
    private void mainPlaceComponents(JPanel panel) {
        /*
         * 这边设置布局为 null
         */
        panel.setLayout(null);

        // 操作类型
        JLabel opType = new JLabel("操作类型：");
        opType.setBounds(30, 20, 100, 25);
        panel.add(opType);

        JComboBox cmb = new JComboBox();    //创建JComboBox
        cmb.setBounds(130, 20, 100, 25);
        cmb.addItem("--请选择--");    //向下拉列表中添加一项
        cmb.addItem("符号化（离线）");
        cmb.addItem("符号化（实时）");
        cmb.addItem("人脸检测");
        panel.add(cmb);

        // 文件输入路径
        fileInButton = new JButton("源文件：");
        fileInButton.setBounds(10, 50, 100, 25);
        panel.add(fileInButton);

        fileInText = new JTextField(20);
        fileInText.setBounds(130, 50, 165, 25);
        panel.add(fileInText);

        // 输出路径
        fileOutButton = new JButton("输出路径：");
        fileOutButton.setBounds(10, 80, 100, 25);
        panel.add(fileOutButton);

        fileOutText = new JTextField(20);
        fileOutText.setBounds(130, 80, 165, 25);
        panel.add(fileOutText);

        // 创建转换按钮
        btnGo = new JButton("开始转换");
        btnGo.setBounds(100, 120, 100, 25);
        panel.add(btnGo);

        //按钮点击事件
        fileInButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //设置选择器
                JFileChooser chooser = new JFileChooser();
                //设为单选
                chooser.setMultiSelectionEnabled(false);
                //是否打开文件选择框
                int returnVal = chooser.showOpenDialog(fileInButton);
                //如果符合文件类型
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String filePath = chooser.getSelectedFile().getAbsolutePath();      //获取绝对路径
                    fileInText.setText(filePath);
                }
            }
        });

        fileOutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //设置选择器
                JFileChooser chooser = new JFileChooser();
                //JFileChooser.FILES_AND_DIRECTORIES 选择路径和文件
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //设为单选
                chooser.setMultiSelectionEnabled(false);
                //打开文件对话框，返回操作类型
                int returnVal = chooser.showOpenDialog(null);
                //如果符合文件类型
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String filePath = chooser.getSelectedFile().getAbsolutePath();      //获取绝对路径
                    fileOutText.setText(filePath);
                }
            }
        });
        
        //执行按钮
        btnGo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (cmb.getSelectedIndex() == 0) {
                    JOptionPane.showMessageDialog(null, "请选择操作类型！", "错误 ", 0);
                    return;
                }
                String inputFile = fileInText.getText();
                String outputFile = fileOutText.getText();

                switch(cmb.getSelectedIndex()){
                    case 1:
                        gotoVideoSymbolization(inputFile,outputFile,panel);
                        break;
                    case 2:
                    	gotoRealtimeSymbolization(outputFile,panel);
                        break;
                    case 3:
                    	gotoPicDetectFace(inputFile,outputFile,panel);
                        break;
                    default:
                        break;
                }
            }
        });
    }
    
    private void operationFileIn(boolean flag) {
    	if(flag) {
    		panel.remove(fileInButton);
        	panel.remove(fileInText);
    	}else{
    		panel.add(fileInButton);
        	panel.add(fileInText);
    	}
    	
    }
    /** 图片人脸识别
    * @param inputFile
    * @param outputFile
    * @param btnGo
    * @param panel
    */
   private void gotoPicDetectFace(String inputFile, String outputFile, JPanel panel){
       if (CommonUtil.isBlank(inputFile)) {
           JOptionPane.showMessageDialog(null, "请选择文件！", "错误 ", 0);
           return;
       }
       StringBuffer sb = new StringBuffer();
       if (CommonUtil.isBlank(outputFile)) {
           sb = sb.append(CommonUtil.getPrefix(inputFile)).append("_");

       } else {
           sb = sb.append(outputFile).append(java.io.File.separator);
       }
       outputFile = sb.append("人脸检测")
               .append(new Date().getTime())
               .append(".")
               .append(CommonUtil.getSuffix(inputFile))
               .toString();
       System.out.println("输出路径："+outputFile);
       try {
           btnGo.setEnabled(false);
           FaceService service = new FaceService();
           new Progress(panel, btnGo, service).start();
           service.detectFace(inputFile, outputFile);
       } catch (Exception ex) {
           ex.printStackTrace();
       }finally {
    	   btnGo.setEnabled(true);
	}
   }
    
    /** 视频符号化-离线
     * @param inputFile
     * @param outputFile
     * @param btnGo
     * @param panel
     */
    private void gotoVideoSymbolization(String inputFile, String outputFile, JPanel panel){
        if (CommonUtil.isBlank(inputFile)) {
            JOptionPane.showMessageDialog(null, "请选择文件！", "错误 ", 0);
            return;
        }
        StringBuffer sb = new StringBuffer();
        if (CommonUtil.isBlank(outputFile)) {
            sb = sb.append(CommonUtil.getPrefix(inputFile)).append("_");

        } else {
            sb = sb.append(outputFile).append(java.io.File.separator);
        }
        outputFile = sb.append("符号化")
                .append(new Date().getTime())
                .append(".")
                .append(CommonUtil.getSuffix(inputFile))
                .toString();
        try {
            btnGo.setEnabled(false);
            VideoRecord videoRecord = new VideoRecord();
            new Progress(panel, btnGo, videoRecord).start();
            videoRecord.frameRecord(inputFile, outputFile, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
        	 btnGo.setEnabled(true);
		}
    }
    
    /** 视频符号化-实时
     * @param inputFile
     * @param outputFile
     * @param btnGo
     * @param panel
     */
    private void gotoRealtimeSymbolization( String outputFile, JPanel panel){
        StringBuffer sb = new StringBuffer();
        if (CommonUtil.isBlank(outputFile)) {
            sb = sb.append(GeneralContants.DESTOP_PATH);
        } else {
            sb = sb.append(outputFile).append(java.io.File.separator);
        }
        outputFile = sb.append("符号化")
                .append(new Date().getTime())
                .append(".")
                .append(CommonUtil.getSuffix("flv"))
                .toString();
        try {
            btnGo.setEnabled(false);
            ImageRecognition.recordCamera(outputFile, Double.valueOf(PropertiesUtil.getInstance().getStringValue("frameRate")));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "错误 ", 0);
        }finally {
        	 btnGo.setEnabled(true);
		}
    }
    
    private class Progress extends Thread {
        JPanel panel;
        JButton button;
        ProgressBar progress;
        Progress(JPanel panel, JButton button, ProgressBar progress) {
            this.panel = panel;
            this.button = button;
            this.progress = progress;
        }

        public void run() {
            //创建一个进度条
            JProgressBar progressBar = new JProgressBar();
            progressBar.setBounds(10, 160, frameWidth - 30, 25);
            progressBar.setStringPainted(true);
            //如果不需要进度上显示“升级进行中...”，可注释此行
            // progressBar.setString("升级进行中...");
            //进度条为确定值
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            panel.add(progressBar);

            int pro = 0;
            for (;;) {
                pro = progress.getProgress();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /**
                 * 设置进度条的值
                 * 当应用程序在事件线程中执行长时间的操作时，会阻塞正常的AWT事件处理，因此阻止了重绘操作的发生
                 * 所以此处经过一次转换
                 */
                Dimension d = progressBar.getSize();
                Rectangle rect = new Rectangle(0, 0, d.width, d.height);
                progressBar.setValue(pro);
                progressBar.paintImmediately(rect);
                if (pro >= 100) {
                    break;
                }
            }
            //progressBar.setValue(imageReceiver.getProgress());
            progressBar.setString("转换完成！");
            button.setEnabled(true);
        }
    }


    public static void main(String[] args) {
        // 显示应用 GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.createAndShowGUI();
            }
        });
    }

}
