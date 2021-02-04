$(function() {
  $('#connect_btn').click(function() {
    const xhr = new XMLHttpRequest();

    xhr.open('POST', '/login');

    xhr.setRequestHeader('content-type', 'application/json');

    const id = $('#name_input').val()
    xhr.send(JSON.stringfy({id: id}));
  })
})
