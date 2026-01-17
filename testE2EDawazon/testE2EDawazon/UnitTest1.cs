using System.Text.RegularExpressions;
using Microsoft.Playwright;

namespace testE2EDawazon;

[Parallelizable(ParallelScope.Self)]
[TestFixture]
public class Tests: E2ETestBase
{
    [Test]
    public async Task AddToCart()
    {
        await Page.GotoAsync("https://www.dawazon.es");
        await CaptureScreenshotAsync("01-page-inicio");
        // usar [alt="user"] para seleccionar por atributo alt
        var userBottom = Page.Locator("[alt='user']");
        // comprobamos que sea el boton correcto y que lleve a la direccion correcta
        await Expect(userBottom).ToHaveAttributeAsync("onclick", "window.location='/auth/signin'");
        await userBottom.ClickAsync();
        
        // comprobamos que estamos en la pagina de inicio de sesion
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/signin"));
        await CaptureScreenshotAsync("02-iniciar-sesion");
        //buscamos los campos a rellenar
        var userName = Page.Locator("[name='userName']");
        var password = Page.Locator("[name='password']");
        
        // metemos datos de ejemplo para iniciar sesion
        await userName.FillAsync("john_doe");
        await password.FillAsync("user");
        await CaptureScreenshotAsync("03-datos-inicio-sesion");
        var iniciarSesion = Page.Locator("[type='submit']");
        await Expect(iniciarSesion).ToHaveTextAsync("Iniciar sesión");
        await iniciarSesion.ClickAsync();
        
        // Esperar a que la navegación complete después del login
        await Expect(Page).ToHaveURLAsync(new Regex("https://www.dawazon.es"));
        await CaptureScreenshotAsync("04-volver-a-inicio");
        // Selector más robusto usando la clase del botón
        var verDetallesButton = Page.Locator("a.btn-amazon").First;
        await verDetallesButton.ClickAsync();
        
        // Esperar a estar en la página de producto
        await Page.WaitForURLAsync(new Regex(".*/products/.*"));
        
        // comprobamos que estamos en la url correcta
        await Expect(Page).ToHaveURLAsync(new Regex(".*/products/.*"));
        await CaptureScreenshotAsync("05-entrar-producto");
        var productName = await Page.Locator("h1.product-title").TextContentAsync();
        
        // buscamos el boton de añadir a carrito
        var carritoButton = Page.Locator("button[onclick*='añadirAlCarrito']");
        
        // clicamos el botoncito
        await carritoButton.ClickAsync();
        
        // comprobamos que se haya metido al carrito
        await Expect(carritoButton).ToContainTextAsync("Quitar del carrito");
        
        var cart = Page.Locator("[href='/auth/me/cart']");
        await cart.ClickAsync();
        
        // comprobamos que estamos en la pagina carrito
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/me/cart"));
        await CaptureScreenshotAsync("06-ver-carrito");
        var productoInCart = Page.Locator($"text= {productName} ");
        await Expect(productoInCart).ToHaveClassAsync(new Regex("product-link"));
    }
    [Test]
    public async Task RemoveFromFav()
    {
        await Page.GotoAsync("https://www.dawazon.es");
        await CaptureScreenshotAsync("01-page-inicio");
        // usar [alt="user"] para seleccionar por atributo alt
        var userBottom = Page.Locator("[alt='user']");
        // comprobamos que sea el boton correcto y que lleve a la direccion correcta
        await Expect(userBottom).ToHaveAttributeAsync("onclick", "window.location='/auth/signin'");
        await userBottom.ClickAsync();
        
        // comprobamos que estamos en la pagina de inicio de sesion
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/signin"));
        await CaptureScreenshotAsync("02-iniciar-sesion");
        //buscamos los campos a rellenar
        var userName = Page.Locator("[name='userName']");
        var password = Page.Locator("[name='password']");
        
        // metemos datos de ejemplo para iniciar sesion
        await userName.FillAsync("john_doe");
        await password.FillAsync("user");
        await CaptureScreenshotAsync("03-datos-inicio-sesion");
        var iniciarSesion = Page.Locator("[type='submit']");
        await Expect(iniciarSesion).ToHaveTextAsync("Iniciar sesión");
        await iniciarSesion.ClickAsync();
        
        // Esperar a que la navegación complete después del login
        await Expect(Page).ToHaveURLAsync(new Regex("https://www.dawazon.es"));
        await CaptureScreenshotAsync("04-volver-a-inicio");
        // Selector más robusto usando la clase del botón
        var verDetallesButton = Page.Locator("a.btn-amazon").First;
        await verDetallesButton.ClickAsync();
        
        // Esperar a estar en la página de producto
        await Page.WaitForURLAsync(new Regex(".*/products/.*"));
        
        // comprobamos que estamos en la url correcta
        await Expect(Page).ToHaveURLAsync(new Regex(".*/products/.*"));
        await CaptureScreenshotAsync("05-entrar-producto");
        var productName = await Page.Locator("h1.product-title").TextContentAsync();
        
        // buscamos el boton de añadir a carrito
        var carritoButton = Page.Locator("button[onclick*='añadirAFav']");
        
        // clicamos el botoncito
        await carritoButton.ClickAsync();
        
        // comprobamos que se haya metido al carrito
        await Expect(carritoButton).ToContainTextAsync("fav");
    }
    [Test]
    public async Task ChangePassword()
    {
        await Page.GotoAsync("https://www.dawazon.es");
        await CaptureScreenshotAsync("01-page-inicio");
        // usar [alt="user"] para seleccionar por atributo alt
        var userBottom = Page.Locator("[alt='user']");
        // comprobamos que sea el boton correcto y que lleve a la direccion correcta
        await Expect(userBottom).ToHaveAttributeAsync("onclick", "window.location='/auth/signin'");
        await userBottom.ClickAsync();
        
        // comprobamos que estamos en la pagina de inicio de sesion
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/signin"));
        await CaptureScreenshotAsync("02-iniciar-sesion");
        //buscamos los campos a rellenar
        var userName = Page.Locator("[name='userName']");
        var password = Page.Locator("[name='password']");
        
        // metemos datos de ejemplo para iniciar sesion
        await userName.FillAsync("jane_smith");
        await password.FillAsync("user");
        await CaptureScreenshotAsync("03-datos-inicio-sesion");
        var iniciarSesion = Page.Locator("[type='submit']");
        await Expect(iniciarSesion).ToHaveTextAsync("Iniciar sesión");
        await iniciarSesion.ClickAsync();
        
        // Esperar a que la navegación complete después del login
        await Expect(Page).ToHaveURLAsync(new Regex("https://www.dawazon.es"));
        await CaptureScreenshotAsync("04-volver-a-inicio");
        // abrirDropdown
        var userDropdown = Page.Locator(".dropdown a[data-bs-toggle='dropdown']");
        await userDropdown.ClickAsync();

        // Esperar a que el menú se muestre
        await Page.WaitForSelectorAsync(".dropdown-menu.show", new() { State = WaitForSelectorState.Visible });
        // Selector usando la clase del botón
        await Page.GetByTestId("Cuenta").ClickAsync();
        // comprobar que estamos en el perfil
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/me"));
        await CaptureScreenshotAsync("05-entrar-cuenta");
        // buscar boton y entrar clicarlo
        await Page.GetByTestId("contraseña").ClickAsync();
        // comprobar que estamos en la pestaña de cambiar contraseña
        await Expect(Page).ToHaveURLAsync(new Regex(".*/auth/me/changepassword"));
        await CaptureScreenshotAsync("06-cambiar-contraseña");
        // buscar inputs con los campòs
        var oldPasswordInput = Page.Locator("[name='oldPassword']");
        var newPasswordInput = Page.Locator("[name='newPassword']");
        var newConfirmPasswordInput = Page.Locator("[name='confirmPassword']");
        // meter los datos 
        await oldPasswordInput.FillAsync("user");
        await newPasswordInput.FillAsync("ejemplo");
        await newConfirmPasswordInput.FillAsync("ejemplo");
        await CaptureScreenshotAsync("07-datos-puestos");
        // buscar el boton para cambiar la contraseña
        var botonConfirmar = Page.Locator("[type='submit']");
        await Expect(botonConfirmar).ToHaveTextAsync("Confirmar");
        // click de button
        await botonConfirmar.ClickAsync();
        // volver a la pagina de inicio
        await Expect(Page).ToHaveURLAsync(new Regex("https://www.dawazon.es"));
        await CaptureScreenshotAsync("08-volver-a-inicio");
    }
}