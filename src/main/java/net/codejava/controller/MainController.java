package net.codejava.controller;

// ----------------------------------------------------------------------------------//

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import net.codejava.helper.Message;
import net.codejava.model.Pending;
import net.codejava.model.User;
import net.codejava.repository.PendingRepo;
import net.codejava.repository.UserRepo;
import net.codejava.service.EmailService;
import net.codejava.service.UserService;


// --------------------------------------------------------------------------------------------- //

@Controller
public class MainController {

	// Beans of other classes so that we can use their methods \\

	@Autowired
	UserRepo repo;

	@Autowired
	UserService userservice;

	@Autowired
	EmailService emailservice;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// ----------------------------------------------------------------------------//

	// After loging in, users will be redirected to their respective pages
	// if admin logs in admin will be redirected to admin page("/users") similarly
	// for users to the user home page("/public/home")

	@RequestMapping("/default")
	public String defaultAfterLogin(HttpServletRequest request) {
		if (request.isUserInRole("ROLE_ADMIN")) {
			return "redirect:/users/";
		}

		return "redirect:/public/home";
	}

	//--------------------------------------------------------------------------------------------------------------//

	// This url will return the home page of our website
	@GetMapping("/index")
	public String signIn() {
		return "index.html";
	}

	// This url will return the news page of our website
	@GetMapping("/news")
	public String news() {
		return "news.html";
	}

	// This url will return the contact page of our website
	@GetMapping("/contact")
	public String contact() {
		return "contact.html";
	}

	
	// 	public ResponseEntity<String> error(){
	// 		System.out.println("Error page................");
	// 		String content =  
    //        "<header>"
    //      + "<h1><span>Url is not reachable from</span></h1>"+"</header>";
	// 	 HttpHeaders responseHeaders = new HttpHeaders();
    // 	responseHeaders.setContentType(MediaType.TEXT_HTML);
	// 	return new ResponseEntity<String>(content,responseHeaders,HttpStatus.BAD_REQUEST);
	// }
	
	@GetMapping("/error")
	public String errormethod(){
		return "error1.html";
	}
	

	// ----------------------REGISTER(GET)--------------------------------------------------------// 

	// This url will be executed when users tries to register
	@GetMapping("/register")
	public String showForm(Model model) {

		// We are using model to store the list of gender and listates
		// These data will be used in the register page

		Pending pending = new Pending();
		model.addAttribute("pending", pending);

		List<String> listgender = Arrays.asList("Choose", "Male", "Female", "Others");
		model.addAttribute("listgender", listgender);

		List<String> liststates = Arrays.asList("Choose", "Andhra Pradesh", "Maharashtra", "Tamil Nadu", "Uttarakhand",
				"West Bengal");
		model.addAttribute("liststates", liststates);

		return "register_new.html";
	}

	// ----------------------REGISTER(POST)--------------------------------------------------------------
	// //

	// This url will be executed when the users fills in all the fields in the
	// register page
	// Since this is a post request, post request = when we are giving data to the
	// server
	// MultipartFile is for fetching any file, Modelattritube indicates the user
	// model, the application
	// will automatically fill the parameters with the details given by user.

	@Autowired
	PendingRepo pendingRepo;

	@PostMapping("/register")
	public String submitForm(@ModelAttribute("pending") Pending pending,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {

		// Checking if username already exists
		if (!(userservice.userExists(pending.getUsername()))) {
			String fileName = multipartFile.getOriginalFilename();

			// String Adhar=user.getUsername();

			pending.setRole("ROLE_USER");

			// decrypting the password before putting it in the db
			pending.setPassword(passwordEncoder.encode(pending.getPassword()));

			// we are also storing the filename in db and storing the file in our local HD
			pending.setPhotos(fileName);

			// saving the user in the database
			// repo is the bean of User repository

			Pending UserPending = pendingRepo.save(pending);
			// Storing the entire path of the file
			String uploadDir = "user-photos/" + UserPending.getUsername();

			// saving the file
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

			System.out.println(pending);
			

		return "redirect:/index";
		}
		// return "index.html";
		else {
			System.out.println("******* User exist **********");
		//if username already exists, then return this page
		return "exist.html";
		}

	}

	// ----------------------UPDATE(POST)--------------------------------------------------------------
	// //

	// Similar to Register url
	// Principle is used to fetch the details of the logged in user and session is
	// used to display message
	// explained later
	@PostMapping("/update")
	public String updateForm(@ModelAttribute("user") User user,
			@RequestParam("image") MultipartFile multipartFile, Principal principle, HttpSession session)
			throws IOException {

		try {
			// fetching the user object from db
			String username = principle.getName();
			User olduser = userservice.getUser(username);

			// If the user has updated the picture
			if (!multipartFile.isEmpty()) {
				String fileName = multipartFile.getOriginalFilename();
				user.setPhotos(fileName);
				String uploadDir = "user-photos/" + olduser.getUsername();

				// delete the old file and save the new one
				FileUtils.deleteDirectory(new File(uploadDir));

				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			} else {

				// if the user is not updating the image then image field must be empty
				// in that case we'll fetch the old image details and insert it into this new
				// user object
				// Reason : If we dont do that, then the image field will remain empty when
				// inserted into the db
				// Thus to preserve the old data, we need to store that into the new object

				user.setPhotos(olduser.getPhotos());
			}

			user.setRole("ROLE_USER");

			User savedUser = repo.save(user);

			System.out.println(user);

			// This is a popup message giving a user feedback about the changes
			session.setAttribute("message", new Message("Your details are updated Successfully", "success"));

		} catch (Exception e) {
			session.setAttribute("message", new Message("Some error occured", "danger"));
			e.printStackTrace();
		}
		return "redirect:/public/home/account";
	}

	// This will return the error page
	// @GetMapping("/error")
	// public String errorpage() {
	// 	return "error.html";
	// }

	// ---------------------send mail from contact us (Bug is there)
	// ------------------------------------------------- //

	@PostMapping("/sendemail")
	public String sendEmail(@RequestParam("fullname") String name, @RequestParam("email") String email,
			@RequestParam("message") String message) {

		// sending email to our email Id with the message entered by the user

		System.out.println("Email: " + email);
		String subject = "Message from " + name + " from Contact Us section";
		String to = "sharereport3@gmail.com";
		boolean f = this.emailservice.sendEmail(subject, message, to);
		System.out.println(f);

		if (f == true)
			return "redirect:/index";
		else
			return "redirect:/contact";

	}

}
