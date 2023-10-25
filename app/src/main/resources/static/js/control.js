//顶部
export function Header({ fullScreen }) {
    return (
      <div className="header-container" style = {{animation: fullScreen ? 'header-slide-up 1000ms forwards' : 'header-slide-down 1000ms forwards' }}>
        <div className="header" style={{  animation: fullScreen ? 'header-slide-up 1000ms forwards' : 'header-slide-down 1000ms forwards' }}>                      
          <div className="pc-header-container">
            <div className="header-left">
              <div className="pc-title-container">
                <div className="title-container">
                  <a href="https://baidu.com" className="header-text header-title1"> {intl.get('header-title1')}  </a>
                </div>
  
              </div>
            </div>
            <div className="header-right">
              <a href="https://www.szu.edu.cn/">
                <img className="header-img2" src={ "/static/img/szu.png"} />
              </a>
              <a href="https://csse.szu.edu.cn/">
                <img className="header-img3" src={ "/static/img/college.png"} />
              </a>
              <a href="https://bigdata.szu.edu.cn/">
                <img className="header-img4" src={ "/static/img/lab.png"} />
              </a>
              
              
            </div>
          </div>
        </div>
      </div>
    );
  }
