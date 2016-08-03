/**
 * The GUI and logic for the application
 *
 * @author Nathan Philliber
 * @version 1.0
 *
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.GregorianCalendar;

public class MBMDisplay extends JFrame{

   //JPanel to contain all elements in the JFrame
   private JPanel panelAll;

   //List of JPanels for each world
   private ArrayList<JPanel> worldPanel =  new ArrayList<JPanel>();

   //File Chooser object
   private JFileChooser explorer = new JFileChooser();

   //List of buttons to backup each world
   private ArrayList<JButton> backupButton = new ArrayList<JButton>();
   
   //Menu elements
   private JMenu[] menus = { new JMenu("File"), new JMenu("Edit"), new JMenu("View")};
   private JMenuItem[] fileItems = { new JMenuItem("Add World"), new JMenuItem("Remove World From List"), new JMenuItem("Move Backup to MC Saves") };
   private JMenuItem[] editItems = { new JMenuItem("Change Backup Location")};
   private JMenuItem[] viewItems = { new JMenuItem("Open Backup Folder"), new JMenuItem("How to Use")};
   
   //Profile object that contains worlds
   private MBMProfile profile = new MBMProfile();
   
   /**
    * Display constructor
    * Sets up JFrame settings, prompt for output directory, etc.
    */
   public MBMDisplay(){
      super(MBMDriver.appName);
      setPreferredSize(new Dimension(600,400));
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      //Setup File Chooser object
      explorer.setDialogTitle("Select Minecraft Save Folder to Backup");
      explorer.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      
      //Make sure that the profile saves when the user closes the window
      addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            profile.save();
         }
      });
      
      //Setup JPanels
      panelAll = new JPanel();
      panelAll.setLayout(new BoxLayout(panelAll, 1));
      
      //Check if there is a save file, if there is, load UI elements for worlds
      if(profile.isNew() == false){
         loadWorlds();
      }

      //Prompt the user for a backup directory, if necessary
      promptForOutputDir(false);
      
      //Start and reload everything
      initializeMenu();
      reloadProfiles();
      
      //Display!
      add(panelAll);
      pack();
      setVisible(true);
   }
   
   /**
    * Add a world/UI
    * @param file The Minecraft world file
    * @param name The name of the Minecraft world
    */
   public void addWorld(File file, String name){
      profile.addWorld(file, name);
      addWorldDisplay(file, name);
      
   }
   
   /**
    * Add the UI for a world
    * @param file The Minecraft world file
    * @param name The name of the Minecraft world
    */
   private void addWorldDisplay(File file, String name){
      JLabel profileLabel = new JLabel(name);
      JLabel dateLabel = new JLabel(" Last Backup: "+profile.getWorld(name).getLastBackupDate().toString());
      backupButton.add(new JButton("Backup"));
      backupButton.get(backupButton.size()-1).addActionListener(listener);
      worldPanel.add(new JPanel());
      worldPanel.get(worldPanel.size()-1).add(profileLabel);
      worldPanel.get(worldPanel.size()-1).add(backupButton.get(backupButton.size()-1));
      worldPanel.get(worldPanel.size()-1).add(dateLabel);
      
      reloadProfiles();
   }

   /**
    * Load UI for all worlds in profile. Intended to be used on startup from a save-file.
    */
   private void loadWorlds(){

      ArrayList<MBMWorld> worlds = profile.getWorldList();
      for(int i = 0; i < profile.numWorlds(); i++){
         addWorldDisplay(worlds.get(i).getWorldFile(), worlds.get(i).getName());
      }
   }
   
   /**
    * Remove UI elements and reset UI lists
    */
   private void killUI(){
      panelAll.removeAll();
      worldPanel =  new ArrayList<JPanel>();
      backupButton = new ArrayList<JButton>();
   }

   /**
    * Prompt the user for an output directory to backup world files to.
    * @param force true if you want the user to choose a directory, even if there already is one setup.
    */
   public void promptForOutputDir(boolean force){

      //Prompt the user if the profile is new or we explicitly want to reprompt
      if(profile.isNew() || force){

         //Setup File Chooser
         String bTitle = explorer.getDialogTitle();
         explorer.setDialogTitle("Select Folder to Save Backups In");
         
         //Keep prompting until the user chooses a directory, or let them cancel if this is a reprompt
         int returnVal = 0;
         do{
            returnVal = explorer.showOpenDialog(MBMDisplay.this);
            
         } while(returnVal != 0 && !force);

         //If the user actually chose a directory, then create a backup folder there.
         if(returnVal == 0){
            explorer.setDialogTitle(bTitle);
            profile.setOutput(explorer.getSelectedFile());

            try{
               if((new File(explorer.getSelectedFile().toPath()+"/MBM_BACKUPS")).exists() == false){
                  Files.createDirectory(Paths.get(explorer.getSelectedFile().toPath()+"/MBM_BACKUPS"));
               }
            } catch(IOException e){
               System.out.println(e);
            }
         }
      }
   }
   
   /**
    * Reload UI elements for everything
    */
   public void reloadProfiles(){

      panelAll.removeAll();
      
      for(int i = 0; i < worldPanel.size(); i++){
         panelAll.add(worldPanel.get(i));
      }
      
      panelAll.revalidate();
      panelAll.repaint();
   }
   
   /**
    * Setup the top menu bar
    */
   private void initializeMenu(){
      //Setup "File" Menu Items
      for (int i = 0; i < fileItems.length; i++) {
         fileItems[i].addActionListener(listener);
         menus[0].add(fileItems[i]);
      }
      //Setup "Edit" Menu Items
      for (int i = 0; i < editItems.length; i++) {
         editItems[i].addActionListener(listener);
         menus[1].add(editItems[i]);
      }
      //Setup "Edit" Menu Items
      for (int i = 0; i < viewItems.length; i++) {
         viewItems[i].addActionListener(listener);
         menus[2].add(viewItems[i]);
      }
      
      JMenuBar mb = new JMenuBar();
      for (int i = 0; i < menus.length; i++){
         mb.add(menus[i]);
      }

      setJMenuBar(mb);
      Container cp = getContentPane();
      cp.setLayout(new FlowLayout());
      
   }

   /**
    * Copy an entire directory
    * Credit to 'smas' for methods to copy a directory
    * http://stackoverflow.com/questions/5368724/how-to-copy-a-folder-and-all-its-subfolders-and-files-into-another-folder
    * @param sourceLocation the directory to be copied
    * @param targetLocation the location to be copied to
    */
   public void copy(File sourceLocation, File targetLocation) throws IOException {
      if (sourceLocation.isDirectory()){
         copyDirectory(sourceLocation, targetLocation);
      } else {
         copyFile(sourceLocation, targetLocation);
      }
   }
   
   /**
    * Copy directory helper method
    * Credit to 'smas' for methods to copy a directory
    * http://stackoverflow.com/questions/5368724/how-to-copy-a-folder-and-all-its-subfolders-and-files-into-another-folder
    * @param source the directory to be copied
    * @param target the location to be copied to
    */
   private void copyDirectory(File source, File target) throws IOException {
      if (!target.exists()){
         target.mkdir();
      }
      
      for (String f : source.list()){
         copy(new File(source, f), new File(target, f));
      }
   }
   
   /**
    * Copy a file
    * Credit to 'smas' for methods to copy a directory
    * http://stackoverflow.com/questions/5368724/how-to-copy-a-folder-and-all-its-subfolders-and-files-into-another-folder
    * @param source the file to be copied
    * @param target the location to be copied to
    */
   private void copyFile(File source, File target) throws IOException {
      try (
       InputStream in = new FileInputStream(source);
       OutputStream out = new FileOutputStream(target)){
         byte[] buf = new byte[1024];
         int length;
         while ((length = in.read(buf)) > 0){
            out.write(buf, 0, length);
         }
      }
   }
   
   /**
    * "File:Add World" operation
    * Add a world + UI to the profile and display
    */
   private void menu_fileAddWorld(){
    
   explorer.setDialogTitle("Select Minecraft Save Folder to Backup");


   //Mac default minecraft saves location
   File saveDir = new File(System.getProperty("user.home")+"/Library/Application Support/minecraft/saves");
   //Check if Mac exists
   if(saveDir.exists()){
       explorer.setCurrentDirectory(saveDir);
   }

   //ADD WINDOWs ------------------------------------------------------------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //Prompt the user to pick a world file
    int returnVal = explorer.showOpenDialog(MBMDisplay.this);
    if(returnVal == JFileChooser.APPROVE_OPTION){
       //See if the World file has a level.dat file inside, if it doesn't then it's not a Minecraft world
       boolean check = new File(explorer.getSelectedFile(), "level.dat").exists();

      if(check){
         String name;
         do{
            name = JOptionPane.showInputDialog(null, "Enter World Name", explorer.getSelectedFile().getName());
         }while(profile.isWorldNameNew(name) == false);

         addWorld(explorer.getSelectedFile(), name);
      }
      else{
            JOptionPane.showMessageDialog(null, "Selected folder does not appear to be a Minecraft world", MBMDriver.appName, JOptionPane.ERROR_MESSAGE);
         }
      }
   }

   /**
    * "File:Remove World" operation
    * Remove a world from the list, does not remove backup files
    */
   private void menu_fileRemoveWorld(){
      String[] options = new String[profile.numWorlds()+1];
      options[0] = "- NONE -";
      for(int i = 0; i < profile.numWorlds(); i++){
         options[i+1] = profile.getWorld(i).getName();
      }

      String answer = (String) JOptionPane.showInputDialog(null,"Select which world to remove",MBMDriver.appName, JOptionPane.WARNING_MESSAGE, null,options, options[0]);

      if(answer == null || answer.equals("- NONE -")){
         return;
      }

      //remove world
      profile.removeWorld(answer);
      killUI();
      loadWorlds();
      reloadProfiles();
      String msg = "<html>Note:<br>This action did not remove your backup files.<br>If you would like to remove your actual backup files, then go to:<br><br>View > Open Backup Folder<br><br>and delete the desired backup folder manually.";
      msg += "</html>";
      JLabel msgLabel = new JLabel(msg, JLabel.CENTER); 
      JOptionPane.showMessageDialog(null, msgLabel, MBMDriver.appName, 
      JOptionPane.PLAIN_MESSAGE);
   }

   /**
    * "View: Open Backups Folder" operation
    * Open the backups folder in system explorer
    */
   private void menu_viewOpenBackups(){
      try{
         Desktop.getDesktop().open(new File(profile.getOutput()+"/MBM_Backups"));
      } catch(IOException e){
         System.out.println(e);
      }
   }

   /**
    * "File: Restore MC Save" operation
    * Copies a backup into the Minecraft Save Folder
    */
   private void menu_fileRestoreSave(){
      //Choose which world to move
      String[] options = new String[profile.numWorlds()+1];
      options[0] = "- NONE -";
      for(int i = 0; i < profile.numWorlds(); i++){
         options[i+1] = profile.getWorld(i).getName();
      }

      String answer = (String) JOptionPane.showInputDialog(null,"Which world would you like to restore?",MBMDriver.appName, JOptionPane.PLAIN_MESSAGE, null,options, options[0]);

      if(answer == null || answer.equals("- NONE -")){
         return;
      }

      //Choose which backup to use

      File backupLoc = new File(profile.getOutput()+"/MBM_Backups/"+answer+"_BACKUPS");

      ArrayList<File> backups = new ArrayList<File>(Arrays.asList(backupLoc.listFiles()));
      
      ArrayList<String> tempOptions = new ArrayList<String>();
      CharSequence notAllowed = ".";
      for(int i = 0; i < backups.size(); i++){
         if(backups.get(i).getName().contains(notAllowed) == false){
            tempOptions.add(backups.get(i).getName());
         }
      }

      options = new String[tempOptions.size()+1];
      options[0] = "- NONE -";

      for(int i = 0; i < tempOptions.size(); i++){
         options[i+1] = tempOptions.get(i);
      }

      answer = (String) JOptionPane.showInputDialog(null,"Which backup would you like to restore?",MBMDriver.appName, JOptionPane.PLAIN_MESSAGE, null,options, options[0]);
      if(answer == null || answer.equals("- NONE -")){
         return;
      }
      //Find minecraft save location

      //Mac default minecraft saves location
      File saveDir = new File(System.getProperty("user.home")+"/Library/Application Support/minecraft/");
      //Check if Mac exists
      if(saveDir.exists()){
         explorer.setCurrentDirectory(saveDir);
      }

//ADD WINDOWs ------------------------------------------------------------------------------>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

      explorer.setDialogTitle("Select Minecraft Saves Folder");
      int returnVal = explorer.showOpenDialog(MBMDisplay.this);
      if(returnVal == JFileChooser.APPROVE_OPTION){
         if(explorer.getSelectedFile().getName().equals("saves")){
            try{
               copy(new File(backupLoc+"/"+answer), new File(explorer.getSelectedFile() + "/"+answer));
            } catch( IOException e){
               System.out.println(e);
            }
         }
         else{
            JOptionPane.showMessageDialog(null, "OPERATION FAILED:\nFailed to select Minecraft saves folder", MBMDriver.appName, JOptionPane.PLAIN_MESSAGE, null);
         }
      }
    }

    /**
    * "View: How to Use" operation
    * Display a tutorial on how to use this application
    */
    private void menu_viewHowTo(){
      String msg = "<html>Welcome to Minecraft Backup Manager<br><br>This tool will help you backup your single player worlds in an easy and organized way.";
      msg += "<br><br>The first thing you need to do is add your worlds. To do this, go to file > Add World. Navigate to your Minecraft<br>";
      msg += "saves folder and select the Minecraft world.<br><br>Whenever you wish to backup your Minecraft world, just press the 'backup' button.";
      msg += "<br><br>To check on your backups, go to view > Open Backups Folder. Find the folder titled with your world name. Inside<br>";
      msg += "there will be your backups. They will be titled 'YEAR'-'MONTH'-'DAY'--'TIME'--'WORLD NAME'";
      msg += "<br><br>To restore a save, go to file > Move Backup to MC Saves, this will not overwrite your other saves.<br>To remove an unwanted save, do so inside the Minecraft client.</html>";
      JLabel msgLabel = new JLabel(msg, JLabel.CENTER); 
      JOptionPane.showMessageDialog(null, msgLabel, MBMDriver.appName, 
      JOptionPane.PLAIN_MESSAGE);
    }

    /**
    * Backup Button
    * Backup the world from the associated button
    */
    private void button_backup(ActionEvent e){
      int buttonNum = -1;

      //Find right button
      for(int i = 0; i < backupButton.size(); i++){
        if(e.getSource() == backupButton.get(i)){
            buttonNum = i;
            String tempWorldName = profile.getWorld(buttonNum).getName();
                     //Check and make sure there is a backup folder for the world, if not make one
            GregorianCalendar copyDate = new GregorianCalendar();
            String backupFolderName = copyDate.get(Calendar.YEAR)+"-"+copyDate.get(Calendar.MONTH)+"-"+copyDate.get(Calendar.DAY_OF_MONTH)+"--"+copyDate.get(Calendar.HOUR_OF_DAY)+"-"+copyDate.get(Calendar.MINUTE)+"-" +copyDate.get(Calendar.SECOND)+"--"+ tempWorldName;

            try{
               try{
                  if((new File(profile.getOutput()+"/MBM_BACKUPS/"+tempWorldName+"_BACKUPS")).exists() == false){
                     Files.createDirectory(Paths.get(profile.getOutput()+"/MBM_BACKUPS/"+tempWorldName+"_BACKUPS"));

                     try{
                        if((new File(profile.getOutput()+"/MBM_BACKUPS/"+tempWorldName+"_BACKUPS/"+backupFolderName)).exists() == false){
                           Files.createDirectory(Paths.get(profile.getOutput()+"/MBM_BACKUPS/"+tempWorldName+"_BACKUPS/"+backupFolderName));
                        }
                     } catch(IOException ex){
                        System.out.println(ex);
                     }
                  }
               } catch(IOException ex){
                  System.out.println(ex);
               }
                        
               copy(profile.getWorld(buttonNum).getWorldFile(), new File(profile.getOutput()+"/MBM_BACKUPS/" + tempWorldName+"_BACKUPS/" + backupFolderName));
                        
               profile.getWorld(buttonNum).backupNow();
               killUI();
               loadWorlds();
           
            } catch(IOException error){
               System.out.println("IO EXCEPTION");
               System.out.println(error);
            }
         }
      }
   }

   /**
    * ActionListener for UI elements
    */
   private ActionListener listener = new ActionListener(){
      public void actionPerformed(ActionEvent e){

         if(e.getSource() == fileItems[1]){
            menu_fileRemoveWorld();
         }

         if(e.getSource() == fileItems[2]){
            menu_fileRestoreSave();
         }

         if(e.getSource() == editItems[0]){
           promptForOutputDir(true);
         }

         if(e.getSource() == viewItems[0]){
           menu_viewOpenBackups();
         }

         if(e.getSource() == viewItems[1]){
           menu_viewHowTo();
         }

         if(e.getSource() == fileItems[0]){
            menu_fileAddWorld();
         }

         if(backupButton.contains(e.getSource())){
            button_backup(e);
         }
      }
   };
}