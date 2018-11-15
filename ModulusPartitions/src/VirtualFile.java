import java.util.ArrayList;
import java.util.Collections;

public class VirtualFile {

    ArrayList<VirtualFileEntry> entries;

    public VirtualFile(){
        entries = new ArrayList<>();
    }

    public void sortFile(){
        Collections.sort(entries);
    }

    public void checkFile(){
        for (int i = 0; i < entries.size(); i++){
            if (entries.get(i).index != i){
                System.out.println("Entry with index " + entries.get(i).index + " in spot " + i);
            }
        }
    }

}
