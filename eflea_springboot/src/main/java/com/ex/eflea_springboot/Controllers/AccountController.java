package com.ex.eflea_springboot.Controllers;

import com.ex.eflea_springboot.helpers.Session;
import com.ex.eflea_springboot.model.Account;
import com.ex.eflea_springboot.model.Title;
import com.ex.eflea_springboot.services.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/account")
public class AccountController {
    private AccountService accountService;
    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    static class LoginAccount {
        public String email;
        public String password;

        public LoginAccount() {}
    }

    static class User {
        public String email;
        public String password;
        public String phone;
        public String username;

        public User() {}
    }

    @Autowired
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @GetMapping("/all")
    @ResponseBody
    public String getAllAccounts(){
        List<Account> accounts = accountService.getAll();
        String ret="";
        for (Account a:accounts)
        {
            ret+=a.toString()+"\n";
        }
        return ret;
    }

    @RequestMapping(path= "/login", method = {RequestMethod.POST, RequestMethod.GET}, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    public void login(@RequestBody LoginAccount login, HttpServletRequest req, HttpServletResponse resp) {

        try {
            Account logged = accountService.login(login.email, login.password);
            if(logged != null){
                Session account = new Session();
                account.email = logged.getEmail();
                account.title = logged.getTitleId();
                account.phone = logged.getPhone();
                account.username = logged.getUsername();
                req.getSession().setAttribute("account", account);
                resp.setStatus(HttpServletResponse.SC_OK);
            } else{
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            logger.error("login failed " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }


    @RequestMapping(path= "/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Session> verify(HttpServletRequest req) {
        Session user = (Session)req.getSession().getAttribute("account");
        if(user == null) {
            return new ResponseEntity<Session>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Session>(user, HttpStatus.OK);
    }


    @RequestMapping(path = "/register", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    public void register(@RequestBody User user, HttpServletResponse resp) {
        try{
            Account account = new Account(user.email, user.password, user.username,
                    Title.USER,new Date(),user.phone);
            accountService.register(account);
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch(Exception e) {
            logger.error("error, " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(path = "/update", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    public void update(@RequestBody User user, HttpServletResponse resp, HttpServletRequest req) {
        if((Session)req.getSession().getAttribute("account") == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try{
            Account account = accountService.updateProfile(user.email, user.phone, user.username);
            Session updated = new Session();
            updated.username = account.getUsername();
            updated.phone = account.getPhone();
            updated.email = account.getEmail();
            updated.title = account.getTitleId();
            req.getSession().setAttribute("account", updated);
        } catch(Exception e) {
            logger.error("update error, " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @PostMapping(path = "/logout")
    public void logout(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
        req.getSession().invalidate();
    }
}
