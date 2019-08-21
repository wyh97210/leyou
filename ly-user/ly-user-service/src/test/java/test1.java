import com.leyou.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


 class errr{
    public static void main(String[] args) {
        UserService userService = new UserService();
        String s = userService.queryUser("1234");
        System.out.println(s);
        return;
    }
}