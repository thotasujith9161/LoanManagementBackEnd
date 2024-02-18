package com.example.demo.UploadingandDownloadingFiles.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileResource {
	//define a location
	public static final String DIRECTORY = System.getProperty("user.home")+"/Downloads/uploads";
	
	//define a method to upload files
	@PostMapping("/upload")
	public ResponseEntity<List<String>> uploadFiles(@RequestParam("files")List<MultipartFile> multipartFiles) throws Exception{
		List<String> filenames = new ArrayList<>();
		for(MultipartFile file : multipartFiles) {
			String filename = StringUtils.cleanPath(file.getOriginalFilename());
			Path fileStorgae = Paths.get(DIRECTORY, filename).toAbsolutePath().normalize();
			Files.copy(file.getInputStream(), fileStorgae, StandardCopyOption.REPLACE_EXISTING);
			filenames.add(filename);
		}
		return ResponseEntity.ok().body(filenames);
		
	}
	
	//define a method to download files
	@GetMapping("download/{filename}")
	public ResponseEntity<Resource> downloadFiles(@PathVariable("filename") String filename) throws IOException{
		
		Path filepath = Paths.get(DIRECTORY).toAbsolutePath().normalize().resolve(filename);
		if(!Files.exists(filepath)) {
			throw new FileNotFoundException(filename + "was not found on the server");
		}
		Resource resource = new UrlResource(filepath.toUri());
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("File-Name", filename);
		httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION,"attachment;File-Name="+ resource.getFilename());
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(filepath))).headers(httpHeaders).body(resource);
	}

}
