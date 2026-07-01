window.initRazorpayPayment = function(orderId, amount, currency) {
    var options = {
        "key": "rzp_test_SeEyCJWZR0HQso", // Replace with your test key ID
        "amount": amount,
        "currency": currency,
        "name": "SubTrackr",
        "description": "Premium Plan - 1 Month",
        "image": "https://cdn-icons-png.flaticon.com/512/5501/5501375.png", // Generic logo
        "order_id": orderId,
        "handler": function (response){
            // Instead of form submission, we can do a fetch post or hidden form submit
            // We will dynamically create a form and submit it to /premium?action=verifyPayment
            
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/SubTrackr/premium?action=verifyPayment';
            
            const pOrderId = document.createElement('input');
            pOrderId.type = 'hidden';
            pOrderId.name = 'razorpay_order_id';
            pOrderId.value = response.razorpay_order_id;
            
            const pPaymentId = document.createElement('input');
            pPaymentId.type = 'hidden';
            pPaymentId.name = 'razorpay_payment_id';
            pPaymentId.value = response.razorpay_payment_id;
            
            const pSignature = document.createElement('input');
            pSignature.type = 'hidden';
            pSignature.name = 'razorpay_signature';
            pSignature.value = response.razorpay_signature;
            
            form.appendChild(pOrderId);
            form.appendChild(pPaymentId);
            form.appendChild(pSignature);
            
            document.body.appendChild(form);
            form.submit();
        },
        "theme": {
            "color": "#6366F1"
        }
    };
    
    var rzp = new Razorpay(options);
    
    rzp.on('payment.failed', function (response){
        window.location.href = '/SubTrackr/payment-failed.jsp';
    });
    
    rzp.open();
};
