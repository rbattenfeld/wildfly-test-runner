package com.six_group.java_ee.common.junit.arquillian;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Validate
 * 
 * Validation utility
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author <a href="mailto:tommy.tynja@diabol.se">Tommy Tynj&auml;</a>
 * @version $Revision: $
 */
public final class Validate
{
   private static Map<Class<? extends Archive<?>>, String> archiveExpressions;

   static
   {
      archiveExpressions = new HashMap<Class<? extends Archive<?>>, String>();
      archiveExpressions.put(JavaArchive.class, ".jar");
      archiveExpressions.put(WebArchive.class, ".war");
      archiveExpressions.put(EnterpriseArchive.class, ".ear");
      archiveExpressions.put(ResourceAdapterArchive.class, ".rar");
   }

   private Validate()
   {
   }

   public static String getArchiveExpression(Class<? extends Archive<?>> type)
   {
      return archiveExpressions.get(type);
   }

   public static boolean archiveHasExpectedFileExtension(final Archive<?> archive)
   {
      final String name = archive.getName();
      for (Map.Entry<Class<? extends Archive<?>>, String> entry : archiveExpressions.entrySet()) {
         if (!name.endsWith(entry.getValue()) && entry.getKey().isInstance(archive))
         {
            return false;
         }
      }
      return true;
   }

   public static boolean isArchiveOfType(Class<? extends Archive<?>> type, Archive<?> archive)
   {
      String expression = getArchiveExpression(type);
      if (expression == null)
      {
         return false;
      }
      return archive.getName().endsWith(expression);
   }

   /**
    * Checks that object is not null, throws exception if it is.
    * 
    * @param obj The object to check
    * @param message The exception message
    * @throws IllegalArgumentException Thrown if obj is null 
    */
   public static void notNull(final Object obj, final String message) throws IllegalArgumentException
   {
      if (obj == null)
      {
         throw new IllegalArgumentException(message);
      }
   }

   /**
    * Checks that the specified String is not null or empty, 
    * throws exception if it is.
    * 
    * @param string The object to check
    * @param message The exception message
    * @throws IllegalArgumentException Thrown if obj is null 
    */
   public static void notNullOrEmpty(final String string, final String message) throws IllegalArgumentException
   {
      if (string == null || string.length() == 0)
      {
         throw new IllegalArgumentException(message);
      }
   }
   
   /**
    * Checks that obj is not null, throws exception if it is.
    * 
    * @param obj The object to check
    * @param message The exception message
    * @throws IllegalStateException Thrown if obj is null
    */
   public static void stateNotNull(final Object obj, final String message) throws IllegalStateException
   {
      if(obj == null)
      {
         throw new IllegalStateException(message);
      }
   }
   
   /**
    * Checks that string is not null and not empty and it represents a path to a valid directory
    * @param string The path to check
    * @param message The exception message
    * @throws RuntimeException Thrown if string is empty, null or it does not represent a path the a valid directory
    */
   public static void configurationDirectoryExists(final String string, final String message)
   {
      if (string == null || string.length() == 0 || new File(string).isDirectory() == false)
      {
         throw new RuntimeException(message);
      }
   }  

}
