/*
 * Keeps track of a save and its' backups.
 *
 * @author Nathan Philliber
 * @version 1.0
 *
 */

import java.util.*;
import java.io.*;

public class MBMWorld{
   
   //The Minecraft world file
   private File worldFile;

   //The GregorianCalendar object of the last backup
   private String lastBackedup;

   //The name of the Minecraft world
   private String name;
   
   /**
    * Get the name of the Minecraft world
    * @return String containing the name
    */
   public String getName(){
      return name;
   }
   
   /**
    * Set the name of the Minecraft world
    * @param the name to be set to
    */
   public void setName(String name){
      this.name = name;
   }
   
   /**
    * Constructor, creates a MBMWorld that represents a Minecraft world save
    * @param worldFile the Minecraft save
    * @param name the Minecraft world name
    */
   public MBMWorld(File worldFile, String name){
      this.worldFile = worldFile;
      setLastBackup("Never");
      setName(name);
   }
   
   /**
    * Get the world file
    * @return the world file
    */
   public File getWorldFile(){
      return worldFile;
   }
   
   /**
    * Get the date of last backup
    * @return the date
    */
   public String getLastBackupDate(){
      return lastBackedup;
   }
   
   /**
    * Set last backup to the current time
    */
   public void backupNow(){
      setLastBackup(new GregorianCalendar());
   }

   /**
    * Set the last backup
    * @param cal the backup time to be set to
    */
   public void setLastBackup(GregorianCalendar cal){

      Locale locale = Locale.getDefault();
      lastBackedup = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale)+ " " + cal.get(Calendar.DAY_OF_MONTH) + ", " + cal.get(Calendar.YEAR) + " at " + cal.get(Calendar.HOUR_OF_DAY) + "." + cal.get(Calendar.MINUTE) + "." + cal.get(Calendar.SECOND);
   }

   public void setLastBackup(String last){
      lastBackedup = last;
   }
   
}