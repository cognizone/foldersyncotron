package cogni.zone.tools.foldersyncotron.app;

import cogni.zone.tools.foldersyncotron.cmdline.FolderSyncotronCommand;
import cogni.zone.tools.foldersyncotron.cmdline.JCommanderBuilder;
import com.beust.jcommander.JCommander;

import java.util.List;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class FolderSyncotron {

  @SuppressWarnings("ThrowCaughtLocally")
  public static void main(String[] args) {
    JCommander jCommander = JCommanderBuilder.build();
    FolderSyncotronCommand folderSyncotronCommand;
    try {
      jCommander.parse(args);
      String command = jCommander.getParsedCommand();
      if (null == command) throw new RuntimeException("No command found");
      List<Object> commandObjects = jCommander.getCommands().get(command).getObjects();
      if (commandObjects.size() != 1) throw new RuntimeException("Not exactly 1 command object found");
      Object commandObject = commandObjects.get(0);
      if (!(commandObject instanceof Runnable)) throw new RuntimeException("Found command object is not a Runnable");
      folderSyncotronCommand = (FolderSyncotronCommand) commandObject;
      folderSyncotronCommand.checkValid();
    }
    catch (Exception exception) {
      System.out.println(exception.getMessage());
      jCommander.usage();
      return;
    }
    folderSyncotronCommand.run();
    System.out.println();
    System.out.println("----------------------------------------------------------------------------");

  }
}
