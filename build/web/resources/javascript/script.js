/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function beginUpload(form){
    window.setInterval(animate, 800);
    
    form.submit();
}

function animate(){
    $.get("UploadStatus", function uploadprogress(data) {
        var percentComplete = data.percent;
        if (percentComplete <= 100) {
            document.getElementById("test").style.width = percentComplete + "%";     
        }
    } );
}



function init(){
    
}

window.onload = init;