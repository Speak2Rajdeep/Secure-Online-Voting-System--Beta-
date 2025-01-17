package net.codejava.controller;

import java.io.File;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import net.codejava.model.Pending;
import net.codejava.model.User;
import net.codejava.repository.PendingRepo;
import net.codejava.repository.UserRepo;
import net.codejava.service.UserService;

@Controller
@RequestMapping("/users")
public class UserSecurityController {
	
	@Autowired
	UserService userService;

	//all users
	@GetMapping("/")
	public String getUsers(Principal principle,Model model){

		String username = principle.getName();
		User user = userService.getUser(username);
		model.addAttribute("currentUser", user);
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "users.html";
	}

	@GetMapping("/pending")
	public String getPendingUsers(Model model){

		
		List<Pending> users = userService.getAllPendingUsers();
		model.addAttribute("pendingUsers", users);
		return "PendingUsers.html";
	}

	
	//return single user
	@GetMapping("/{userName}")
	public User getUser(@PathVariable("userName") String userName) {
		return this.userService.getUser(userName);
	}
	
	// when update photo delete previous photo from directory
	@GetMapping("/removeFile/{username}/{fileName}")
	public String removeFileHandler (@PathVariable("username") String username, @PathVariable("fileName") String fileName, Model model) {		
		//ModelAndView mav = new ModelAndView("redirect:/image-upload/employees");
		String path = null;
		File file =null;

		try{
			path = "user-photos/" + username;
			System.out.println(path);
			file=new File(path);
			if(file.exists())
			{
				boolean status = userService.deleteUser(username,fileName,path);
				List<User> users = userService.getAllUsers();
				model.addAttribute("users", users);
				return "redirect:/users/";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/users/";
	}


	@Autowired
	PendingRepo pendingRepo;

	@Autowired
	UserRepo repo;

	@GetMapping("/approveFile/{username}/{fileName}")
	public String approveFileHandler (@PathVariable("username") String username, @PathVariable("fileName") String fileName, Model model) {		
		//ModelAndView mav = new ModelAndView("redirect:/image-upload/employees");

		try{
			Pending puser = userService.getPendingUser(username);
			User user=new User();
			user.setAddress(puser.getAddress());
			user.setUsername(username);
			user.setBirthday(puser.getBirthday());
			user.setCity(puser.getCity());
			user.setEmail(puser.getEmail());
			user.setFirstname(puser.getFirstname());
			user.setGender(puser.getGender());
			user.setLastname(puser.getLastname());
			user.setMobileno(puser.getMobileno());
			user.setPassword(puser.getPassword());
			user.setPhotos(puser.getPhotos());
			user.setZip(puser.getZip());
			user.setState(puser.getState());
			user.setRole(puser.getRole());

			repo.save(user);

			pendingRepo.deleteById(username);
			List<Pending> users = userService.getAllPendingUsers();
			model.addAttribute("users", users);
			return "redirect:/users/pending";
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/users/";
	}
		
}
