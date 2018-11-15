public class VirtualFileObject {

    VirtualFile connectedFile;
    int currentIndex;

    public VirtualFileObject(VirtualFile file){
        connectedFile = file;
        currentIndex = 0;
    }

    public void goToIndex(int index){ //implemented with lseek
        currentIndex = index;
    }

    public VirtualFileEntry getNext(){
        VirtualFileEntry readValue = connectedFile.entries.get(currentIndex);
        currentIndex++;
        return readValue;
    }

    public boolean eof(){
        return (currentIndex >= connectedFile.entries.size());
    }
}
