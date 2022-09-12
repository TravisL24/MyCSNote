public class MusicPhone extends PhoneDecorate{

    public MusicPhone(Phone phone) {
        super(phone);
    }

    public void listenMusic() {
        System.out.println("独立扩展的muisc方法");
    }

    @Override
    public void call() {
        // 在执行call之前拓展
        listenMusic();
        super.call();
    }
}
