# seddit
Simple reddit clone written in Scala with: Play 2.4 + Slick 3.0 + Postgresql.
Created mainly to test play and slick frameworks.

### Installation

### Clone repo
```
git clone https://github.com/awronski/seddit.git
```

### Create database
```sql
CREATE USER ali;
ALTER ROLE ali PASSWORD 'password';
CREATE DATABASE alidb OWNER ali ENCODING = 'UTF-8';
```
Schema will be created automatically during startup.

### Configure
- Rename `application.conf.template` to `application.conf`
- Fill in the missing values in the configuration
- Application uses Amazon Simple Starage, so fill the access tokens too.

### Run
- Build app: `activator dist`
- Run: `./seddit-1.0.0-SNAPSHOT/bin/seddit -Dconfig.file=/your_path/application.conf -J-server`

# Capabilities
- Register (captcha), Login, Remember me, Logout
- Create a post with text, link, pictures and tags
- Vote for posts
- Add comments to the posts

# Screenshot
![alt screenshot](https://raw.githubusercontent.com/awronski/seddit/master/post.jpg)

License
=======

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.