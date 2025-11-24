-- 샘플 데이터 삽입
USE dagachi;

INSERT INTO postings (title, description, type, created_at) VALUES
('React 웹 개발 프로젝트', 'React와 Spring Boot를 활용한 풀스택 웹 개발 프로젝트입니다. 함께 성장할 팀원을 찾습니다!', 'PROJECT', NOW() - INTERVAL 1 DAY),
('알고리즘 스터디', '매주 알고리즘 문제를 풀고 리뷰하는 스터디입니다. 코딩테스트 준비에 최적화되어 있습니다.', 'STUDY', NOW() - INTERVAL 2 DAY),
('AI 챗봇 개발', 'ChatGPT API를 활용한 맞춤형 챗봇 서비스를 개발합니다. AI에 관심있는 분 환영합니다!', 'PROJECT', NOW() - INTERVAL 3 DAY),
('Java 백엔드 스터디', 'Spring Framework를 깊이 있게 학습하는 스터디입니다. 실전 프로젝트도 함께 진행합니다.', 'STUDY', NOW() - INTERVAL 4 DAY),
('모바일 앱 개발 프로젝트', 'Flutter를 사용한 크로스플랫폼 앱 개발 프로젝트입니다. 디자이너와 협업 예정입니다.', 'PROJECT', NOW() - INTERVAL 5 DAY),
('클린코드 스터디', '클린코드와 리팩토링을 주제로 한 스터디입니다. 코드 리뷰를 통해 함께 성장합니다.', 'STUDY', NOW() - INTERVAL 6 DAY);
