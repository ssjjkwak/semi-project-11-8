package org.kosta.sharecipe.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.kosta.sharecipe.common.FileManager;
import org.kosta.sharecipe.model.CategoryVO;
import org.kosta.sharecipe.model.MemberVO;
import org.kosta.sharecipe.model.RecipeDAO;
import org.kosta.sharecipe.model.RecipeVO;

public class CreateRecipeController implements Controller {

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//로그인 체크
		/*
		HttpSession session = request.getSession(false);
		if(session ==null ||session.getAttribute("mvo")==null)
			return "redirect:ListController.do";
		*/
		
		//이미지 삽입에 관련된 사항들
		String saveDir="C:/kosta224/toy-project/semi-project-11-8/src/main/webapp/image"; //저장될 위치 설정
		int maxSize=3*1024*1024; //3M byte 로 크기를 제한
		
		DiskFileItemFactory itemFactory=new DiskFileItemFactory();
		itemFactory.setRepository(new File(saveDir));
		itemFactory.setSizeThreshold(maxSize); 
		itemFactory.setDefaultCharset("utf-8");
		
		ServletFileUpload upload=new ServletFileUpload(itemFactory); //위에 설정한 정보를 파일업로드에 넣어줌
		
		//업로드된 정보 분석!!! 각각의 컴포넌트들을  FileItem 단위로 쪼갠다..
		request.setCharacterEncoding("utf-8"); //다국어 인코딩
		
		//java.lang.ClassCastException: org.apache.catalina.connector.RequestFacade cannot be cast to org.apache.tomcat.util.http.fileupload.RequestContext
		// 이슈 기록대상
		//List<FileItem> items=upload.parseRequest(request);
		//List<FileItem> items=upload.parseRequest((RequestContext) request);
		List<FileItem> items  = upload.parseRequest(new ServletRequestContext(request));
		
		RecipeVO recipeVO = new RecipeVO();//Empty상태의 VO 생성 
		
		for(FileItem item : items){
			if(item.isFormField()){ //textfield 라면...db에 넣어야지
				//vo 에 텍스트필드들의 값을 담자!!
				if(item.getFieldName().equals("category_num")){
					//카테고리 부분 해결해야함
					CategoryVO categoryVO = new CategoryVO();
					int category_num = Integer.parseInt(item.getString());
					categoryVO.setCategoryNo(category_num);
					recipeVO.setCategoryVO(categoryVO);
				}else if(item.getFieldName().equals("title")){
					recipeVO.setTitle(item.getString());
				}else if(item.getFieldName().equals("content")){
					recipeVO.setContent(item.getString());
				}else if(item.getFieldName().equals("id")){
					//session 처리 해결
					String id = "JAVA";
					MemberVO mvo = new MemberVO();
					mvo.setId(id);
					recipeVO.setMemberVO(mvo);
				}
			}else{ // textfield가 아니라면..업로드 처리
				String newName=System.currentTimeMillis()+"."+FileManager.getExtend(item.getName()); //png , jpg
				String destFile = saveDir+"/"+newName; //image/사진1.png
				File file = new File(destFile);
				item.write(file);//물리적 저장 시점
				
				System.out.println("업로드 완료");
				recipeVO.setImage(newName);//vo 에 파일명 값을 담아줌
			}
		}
		
		int result = RecipeDAO.getInstance().createRecipe(recipeVO);
		if(result==0){
			System.out.println("등록실패");
			return "redirect:board/recipe-write-fail.jsp";
		}else{
			System.out.println("등록성공");
			return "redirect:RecipeListController.do";
		}
		
		//아래와 같은 방법을 사용할 수 없던 이유 정훈이한테 설명해주기
		/*
		String category = request.getParameter("category");
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String image = request.getParameter("image");
		
		//로그인하지 않고 구현 실행해보기 위함임
		String id = "JAVA";
		
		RecipeVO recipeVO = new RecipeVO();
		recipeVO.setCategoryVO(new CategoryVO(0, category));
		recipeVO.setTitle(title);
		recipeVO.setContent(content);
		recipeVO.setImage(image);
		//recipeVO.setMemberVO((MemberVO)session.getAttribute("mvo"));
		
		//위에 session 으로 전부 변경되어야함
		MemberVO mvo = new MemberVO();
		mvo.setId(id);
		recipeVO.setMemberVO(mvo);
		//----------------------------------------
		
		//int result = RecipeDAO.getInstance().createRecipe(recipeVO);
		*/
		
	}
}
