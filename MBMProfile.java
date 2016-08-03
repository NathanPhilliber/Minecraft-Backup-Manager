/*
 * Contains information about saves and backups
 *
 * @author Nathan Philliber
 * @version 1.0
 *
 */

import java.util.*;
import java.io.*;

public class MBMProfile{
   
   //List of the worlds to be kept track of
   public ArrayList<MBMWorld> worlds;

   //The directory where backups are stored
   private File outputDir;

   //A boolean to keep track of whether or not the profile is loaded from a save or not
   private boolean isNew = true;
   
   /**
    * Constructor, initializes the world list and tries to load the save
    */
   public MBMProfile(){
      worlds = new ArrayList<MBMWorld>();
      loadSave();
   }
   
   /**
    * Add a world (no UI)
    * @param file the Minecraft world file to be backed up
    * @param name the name of the Minecraft world
    */
   public void addWorld(File file, String name){
      worlds.add(new MBMWorld(file, name));
   }
   
   /**
    * Set the output directory for the profile
    * @param file the new output directory
    */
   public void setOutput(File file){
      outputDir = file;
   }
   
   /**
    * Get the number of worlds in the profile
    * @return number of worlds
    */
   public int numWorlds(){
      return worlds.size();
   }
   
   /**
    * Get the list of worlds
    * @return Returns an ArrayList<MBMWorld>
    */
   public ArrayList<MBMWorld> getWorldList(){
      return worlds;
   }
   
   /**
    * Get the output directory
    * @return the output directory file
    */
   public File getOutput(){
      return outputDir;
   }
   
   /**
    * Attempt to load from the data.MBM file
    */
   public void loadSave(){
      if((new File("data.MBM")).exists()){
         isNew = false;

         try {
            BufferedReader br = new BufferedReader(new FileReader("data.MBM"));
            String line = br.readLine();
            
            while (line != null) {
               
               if(line.contains("outDir")){
                  String[] result = line.split(":");
                  outputDir = new File(result[1]);
                  if(outputDir.exists() == false){
                     isNew = true;
                  }
               }
               else if(line.contains("MBMWORLD")){
                  String[] result = line.split(":");
                  //1 = filename
                  //2 = world name
                  //3 = path
                  //4 = date
                  addWorld(new File(result[3]), result[2]);
                  worlds.get(numWorlds()-1).setLastBackup(result[4]);
               }
               
               line = br.readLine();
            }
            
            br.close();
            
         } catch(IOException e){
            System.out.println(e);
         }
      }
   }
   
   /**
    * Find out if the profile was just created or loaded from the save file
    * @return true if the profile was just created, false otherwise
    */
   public boolean isNew(){
      return isNew;
   }
   
   /**
    * Write data to the data.MBM file
    */
   public void save(){
      try{
         Date date = new Date();
         
         PrintWriter writer = new PrintWriter("data.MBM", "UTF-8");
         
         writer.println("version:"+MBMDriver.version);
         writer.println("numworlds:" + worlds.size());
         writer.println("lastclosed:" + date.toString());
         writer.println("outDir:"+outputDir.toPath());
         
         for(int i = 0; i < worlds.size(); i++){
            
            writer.println("MBMWORLD:"+worlds.get(i).getWorldFile().getName()+":"+worlds.get(i).getName()+":"+worlds.get(i).getWorldFile().toPath()+":"+worlds.get(i).getLastBackupDate());
         }
         
         writer.close();
      }catch(FileNotFoundException e){
         System.out.println("ERROR: Failed to Find File");
      }catch(UnsupportedEncodingException e){
         System.out.println("ERROR: Unsupported Encoding Exception");
      }
   }
   
   /**
    * Get a world from the profile list of worlds
    * @param num the index of the world to be returned
    * @return an MBMWorld
    */
   public MBMWorld getWorld(int num){
      return worlds.get(num);
   }

   public MBMWorld getWorld(String name){
      for(int i = 0; i < numWorlds(); i++){
         if(worlds.get(i).getName().equals(name)){
            return getWorld(i);
         }
      }
      return null;
   }

   /**
    * Check to see if the minecraft world name is already in use
    * @param name of the minecraft world
    * @return true if the world name is unique
    */
   public boolean isWorldNameNew(String name){
      for(int i = 0; i < worlds.size(); i++){
         if(worlds.get(i).getName().equals(name)){
            return false;
         }
      }
      return true;
   }

   /**
    * Remove a world from the worlds list
    * @param name the name of the world to remove
    */
   public void removeWorld(String name){
      worlds.remove(getWorld(name));
   }
}